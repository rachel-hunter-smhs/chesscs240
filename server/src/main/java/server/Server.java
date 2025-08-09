package server;

import spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.Response;
import spark.Spark;

public class Server {
    private final Gson gson = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        var dao  = new MemoryDataAccess();
        var users = new UserService(dao);
        var games = new GameService(dao);
        var clear = new ClearService(dao);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
