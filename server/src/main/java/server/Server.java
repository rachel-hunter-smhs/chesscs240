package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import service.ClearService;
import service.GameService;
import service.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;


public class Server {
    private final Gson gson = new Gson();
    private final Javalin javalin;
    private record NameOnly(String gameName) {}
    private record JoinBody(String playerColor, int gameID) {}

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
}
