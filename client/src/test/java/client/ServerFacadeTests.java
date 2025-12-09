package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

   @BeforeEach
    void clearDB() {
        try {
            facade.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void registerPositive() throws Exception{
        var authData = facade.register("player1", "password", "p1@email.com");
        Assertions.assertNotNull(authData);
        Assertions.assertTrue(authData.authToken().length()>10);
    }
    @Test
    public void loginPositive() throws Exception{
        facade.register("player2", "password", "p2@email.com");
        var authData = facade.login("player2", "password");
        Assertions.assertNotNull(authData);
        Assertions.assertNotNull(authData.authToken());

    }

}