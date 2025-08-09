import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var server = new Server();
        var port = server.run(8080);
        System.out.printf("♕ 240 Chess Server running on port %d%n", port);
    }
}