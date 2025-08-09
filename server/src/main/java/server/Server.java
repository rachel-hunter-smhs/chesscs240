package server;

import server.dataaccess.MemoryDataAccess;
import server.service.ClearService;
import spark.Spark;

public class Server {
    public int run(int port) {
        Spark.port(port);
        Spark.staticFiles.location("web");

        var dao = new MemoryDataAccess();
        var clear = new ClearService(dao);

        Spark.delete("/db", (req, res) -> { clear.clear(); res.status(200); return "{}"; });
        Spark.get("/ping", (req, res) -> "pong");

        Spark.awaitInitialization();
        return Spark.port();
    }
    public void stop(){ Spark.stop(); Spark.awaitStop(); }
}
