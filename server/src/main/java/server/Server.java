package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server.dataaccess.DataAccessException;
import server.dataaccess.MemoryDataAccess;
import server.dataaccess.MySQLDataAccess;
import server.service.ClearService;
import server.service.UserService;
import spark.Response;
import spark.Spark;
import server.service.GameService;

public class Server {
    private final Gson gson = new Gson();

    public int run(int port){
        Spark.port(port);
        Spark.staticFiles.location("web");

        var dao   = new MySQLDataAccess();
        var clear = new ClearService(dao);
        var users = new UserService(dao);
        var games = new GameService(dao);

        Spark.delete("/db",(req,res)-> ok(res,()->{ clear.clear(); return new JsonObject(); }));

        Spark.post("/user",(req,res)-> handle(res,
                ()-> users.register(gson.fromJson(req.body(), UserService.RegisterRequest.class))));

        Spark.post("/session",(req,res)-> handle(res,
                ()-> users.login(gson.fromJson(req.body(), UserService.LoginRequest.class))));

        Spark.delete("/session",(req,res)-> ok(res,()->{
            users.logout(new UserService.LogoutRequest(req.headers("authorization")));
            return new JsonObject();
        }));
        Spark.get("/game",(req,res)-> handle(res,
                ()-> games.list(new GameService.ListRequest(req.headers("authorization")))));

        record NameOnly(String gameName){}
        Spark.post("/game",(req,res)-> handle(res, ()-> {
            var b = gson.fromJson(req.body(), NameOnly.class);
            return games.create(new GameService.CreateRequest(b.gameName(), req.headers("authorization")));
        }));

        record JoinBody(String playerColor,int gameID){}
        Spark.put("/game",(req,res)-> ok(res, ()-> {
            var b = gson.fromJson(req.body(), JoinBody.class);
            games.join(new GameService.JoinRequest(req.headers("authorization"), b.playerColor(), b.gameID()));
            return new JsonObject();
        }));

        Spark.awaitInitialization();
        return Spark.port();

    }

    public void stop(){ Spark.stop(); Spark.awaitStop(); }

    private interface X<T>{ T get() throws Exception; }

    private String handle(Response res, X<?> f){
        try{ res.status(200); return gson.toJson(f.get()); }
        catch(DataAccessException e){ return mapped(res,e.getMessage()); }
        catch(Exception e){ res.status(500); return "{\"message\":\"Error: "+safe(e.getMessage())+"\"}"; }
    }
    private String ok(Response res, X<?> f){
        try{ f.get(); res.status(200); return "{}"; }
        catch(DataAccessException e){ return mapped(res,e.getMessage()); }
        catch(Exception e){ res.status(500); return "{\"message\":\"Error: "+safe(e.getMessage())+"\"}"; }
    }
    private String mapped(Response res,String m){
        switch(m){
            case "bad request" -> res.status(400);
            case "unauthorized" -> res.status(401);
            case "already taken" -> res.status(403);
            default -> res.status(500);
        }
        return "{\"message\":\"Error: " + m + "\"}";
    }
    private String safe(String s){ return s==null? "" : s.replace("\"","'"); }
}
