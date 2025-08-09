package server;
//import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import server.dataaccess.MemoryDataAccess;
import service.ClearService;
//import service.GameService;
//import service.UserService;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        var dao = new MemoryDataAccess();

        // Register your endpoints and handle exceptions here.

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
