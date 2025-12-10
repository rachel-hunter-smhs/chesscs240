package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import model.GameData;
import server.websocket.Connections;
import service.ClearService;
import service.GameService;
import service.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import websocket.commands.CommandMakeMove;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import org.eclipse.jetty.websocket.api.Session;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Server {
    private final Gson gson = new Gson();
    private final Javalin javalin;
    private  final Connections connect = new Connections();
    private final MySQLDataAccess dao = new MySQLDataAccess();
    private record NameOnly(String gameName) {}
    private record JoinBody(String playerColor, int gameID) {}
    private final Set<Integer> resignedGames = ConcurrentHashMap.newKeySet();


    public Server(){
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        var dao   = new MySQLDataAccess();
        var clear = new ClearService(dao);
        var users = new UserService(dao);
        var games = new GameService(dao);
        javalin.delete("/db", ctx -> ok(ctx, () -> {
            clear.clear();
            return new JsonObject();
        }));

        javalin.post("/user", ctx -> handle(ctx,
                () -> users.register(gson.fromJson(ctx.body(), UserService.RegisterRequest.class))));

        javalin.post("/session", ctx -> handle(ctx,
                () -> users.login(gson.fromJson(ctx.body(), UserService.LoginRequest.class))));

        javalin.delete("/session", ctx -> ok(ctx, () -> {
            users.logout(new UserService.LogoutRequest(ctx.header("authorization")));
            return new JsonObject();
        }));

        javalin.get("/game", ctx -> handle(ctx,
                () -> games.list(new GameService.ListRequest(ctx.header("authorization")))));

        javalin.post("/game", ctx -> handle(ctx, () -> {
            var b = gson.fromJson(ctx.body(), NameOnly.class);
            return games.create(new GameService.CreateRequest(b.gameName(), ctx.header("authorization")));
        }));

        javalin.put("/game", ctx -> ok(ctx, () -> {
            var b = gson.fromJson(ctx.body(), JoinBody.class);
            games.join(new GameService.JoinRequest(ctx.header("authorization"), b.playerColor(), b.gameID()));
            return new JsonObject();
        }));

    }
    public int run(int wantedPort) {
        javalin.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("WebSocket connected: " + ctx.session.getRemoteAddress());
            });

            ws.onMessage(ctx -> {
                System.out.println("Received message: " + ctx.message());

                try {
                    com.google.gson.JsonObject jsonObject = gson.fromJson(ctx.message(), com.google.gson.JsonObject.class);
                    String commandTypeStr = jsonObject.get("commandType").getAsString();

                    UserGameCommand command = null;
                    switch (commandTypeStr){
                        case "MAKE_MOVE" -> command = gson.fromJson(ctx.message(), CommandMakeMove.class);
                        case "CONNECT", "LEAVE", "RESIGN" -> command = gson.fromJson(ctx.message(), UserGameCommand.class);
                        default ->  {
                            ctx.send(gson.toJson(new ErrorMessage("Unknown command type")));
                        }
                    }
                    if(command != null){
                        switch (command.getCommandType()){
                            case CONNECT ->  doConnect(ctx, command, connect);
                            case MAKE_MOVE ->  doMakeMove(ctx, command, connect);
                            case LEAVE->  doLeave(ctx, command, connect);
                            case RESIGN ->  doResign(ctx, command, connect);


                        }

                    }

                } catch (Exception e){
                    String oopSJSon = gson.toJson(new websocket.messages.ErrorMessage(e.getMessage()));
                    ctx.send(oopSJSon);
                }
            });

            ws.onClose(ctx -> {
                System.out.println("WebSocket closed");
            });

            ws.onError(ctx -> {
                System.err.println("WebSocket error: " + (ctx.error() != null ? ctx.error().getMessage() : "unknown"));
            });
        });

        javalin.start(wantedPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private interface X<T> { T get() throws Exception; }

    private void handle(Context ctx, X<?> f) {
        try {
            ctx.status(200);
            ctx.result(gson.toJson(f.get()));
        } catch (DataAccessException e) {
            mapped(ctx, e.getMessage());
        } catch (Exception e) {
            ctx.status(500);
            ctx.result("{\"message\":\"Error: " + safe(e.getMessage()) + "\"}");
        }
    }

    private void ok(Context ctx, X<?> f) {
        try {
            f.get();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            mapped(ctx, e.getMessage());
        } catch (Exception e) {
            ctx.status(500);
            ctx.result("{\"message\":\"Error: " + safe(e.getMessage()) + "\"}");
        }
    }

    private void mapped(Context ctx, String m) {
        switch (m) {
            case "bad request" -> ctx.status(400);
            case "unauthorized" -> ctx.status(401);
            case "already taken" -> ctx.status(403);
            default -> ctx.status(500);
        }
        ctx.result("{\"message\":\"Error: " + m + "\"}");
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
    private void doConnect(io.javalin.websocket.WsMessageContext ctx, UserGameCommand command, Connections connect) throws Exception {
        String username = getUsername(command.getAuthToken());

        int gameID =command.getGameID();
        GameData gameData = dao.getGame(gameID);
        ChessGame game = gameData.game();
        if(game == null){
            sendError(ctx.session, "No Game :(");
            return;
        }

        connect.add(gameID, ctx.session);
        String loadGameJson = gson.toJson(new LoadGameMessage(game));
        ctx.send(loadGameJson);
        String notify = gson.toJson(new websocket.messages.NotificationMessage( username +" joined"));
        connect.broadcast(gameID, notify, ctx.session);

    }
    private void doMakeMove(io.javalin.websocket.WsMessageContext ctx, UserGameCommand command, Connections connect) throws Exception {
        String username = getUsername(command.getAuthToken());

        int gameID =command.getGameID();
        GameData gameData = dao.getGame(gameID);
        ChessGame game = gameData.game();
        if(game == null){
            sendError(ctx.session, "No Game :(");
            return;
        }

       if(!(command instanceof CommandMakeMove)){
           sendError(ctx.session, "Invalid command");
           return;
       }
       CommandMakeMove moveGo = (CommandMakeMove) command;
       chess.ChessMove move = moveGo.getChessMove();
        if (move == null) {
            sendError(ctx.session, "Move was null");
            return;
        }
        ChessGame.TeamColor playerColor = null;
        if(username.equals(gameData.whiteUsername())){
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        } else{
            sendError(ctx.session, "you are not a player :-P");
            return;
        }
        if ((game.getTeamTurn() != playerColor)){
            sendError(ctx.session, "Not your turn B-)");
            return;
        }
        try {
            game.makeMove(move);
            dao.saveGame(gameData);
            String loadGameJson = gson.toJson(new LoadGameMessage(game));
            connect.broadcast(gameID, loadGameJson, null);
            ChessGame.TeamColor opp = (playerColor == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            if (game.isInCheckmate(opp)){
                String winner = gson.toJson((new NotificationMessage(username +"wins by checkmate! YAY")));
                connect.broadcast(gameID, winner, null);
            } else if (game.isInStalemate(opp)) {
                String tie = gson.toJson((new NotificationMessage("Stalemate :( no winners")));
                connect.broadcast(gameID, tie, null);

            } else if (game.isInCheck(opp)) {
                String checkUrself = gson.toJson((new NotificationMessage(opp +"is in check")));
                connect.broadcast(gameID, checkUrself, null);


            }
        } catch (chess.InvalidMoveException e) {
            sendError(ctx.session, "Invalid move");
        }

    }
    private void doLeave(io.javalin.websocket.WsMessageContext ctx, UserGameCommand command, Connections connect) throws Exception {
        String username = getUsername(command.getAuthToken());
        int gameID =command.getGameID();
        GameData gameData = dao.getGame(gameID);
        ChessGame game = gameData.game();
        if(game == null){
            sendError(ctx.session, "No Game :(");
            return;
        }
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();
        if (username.equals(white)) {
            white = null;
        } else if (username.equals(black)) {
            black = null;
        }
        GameData update = new GameData(gameData.gameID(), white, black, gameData.gameName(), gameData.game());
        dao.saveGame(update);
        connect.remove(gameID, ctx.session);

        String tellDaWorld = gson.toJson(new NotificationMessage(username + "left"));
        connect.broadcast(gameID, tellDaWorld, null);
    }
    private void doResign (io.javalin.websocket.WsMessageContext ctx, UserGameCommand command, Connections connect) throws Exception{
        String username = getUsername(command.getAuthToken());
        int gameID =command.getGameID();
        GameData gameData = dao.getGame(gameID);
        ChessGame game = gameData.game();
        if(game == null){
            sendError(ctx.session, "No Game :(");
            return;
        }
        boolean isPlayer = username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername());
        if(!isPlayer){
            sendError(ctx.session, "you are not a player :P");
            return;

        }
        if (resignedGames.contains(gameID)) {
            sendError(ctx.session, "Game already over.");
            return;
        }
        resignedGames.add(gameID);

        String Quitter = gson.toJson(new NotificationMessage(username + " quit. GAME OVER...."));
        connect.broadcast(gameID, Quitter, null);

    }
    private void sendError(Session sesh, String oops){
        try{
            String oopsJson = gson.toJson(new websocket.messages.ErrorMessage(oops));
            sesh.getRemote().sendString(oopsJson);
        } catch(Exception e){
            System.err.println("Failed to send error" + e.getMessage());
        }
    }
    private String getUsername (String authToken) throws Exception{
        var authData = dao.getAuth(authToken);
        if(authData == null){
            throw  new Exception("Invalid");
        }
        return authData.username();
    }




}
