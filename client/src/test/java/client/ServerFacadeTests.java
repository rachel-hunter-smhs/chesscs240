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

   /*@BeforeEach
    void clearDB() {
        try {
            facade.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    */

    @Test
    public void registerPositive() throws Exception{
        var authData = facade.register("player1", "password", "p1@email.com");
        Assertions.assertNotNull(authData);
        Assertions.assertNotNull(authData.authToken());
        Assertions.assertEquals("player1",authData.username());
        Assertions.assertTrue(authData.authToken().length()>10);
    }
    @Test
    public void loginPositive() throws Exception{
        facade.register("player2", "password", "p2@email.com");
        var authData = facade.login("player2", "password");
        Assertions.assertNotNull(authData);
        Assertions.assertNotNull(authData.authToken());

    }
    @Test
    void registerNegativeDuplicateUser() throws Exception {
        facade.register("user1", "pass", "email@example.com");
        Exception ex = Assertions.assertThrows(Exception.class, () ->
                facade.register("user1", "pass", "email@example.com")
        );
        Assertions.assertTrue(ex.getMessage().toLowerCase().contains("already")
                || ex.getMessage().toLowerCase().contains("taken")
                || ex.getMessage().toLowerCase().contains("exists"));
    }
    @Test
    void loginNegativeBadPassword() throws Exception {
        facade.register("user1", "pass", "email@example.com");
        Exception ex = Assertions.assertThrows(Exception.class, () ->
                facade.login("user1", "wrong")
        );
        Assertions.assertTrue(ex.getMessage().toLowerCase().contains("unauthorized")
                || ex.getMessage().toLowerCase().contains("error")
                || ex.getMessage().toLowerCase().contains("incorrect"));
    }

    @Test
    void logoutPositive() throws Exception {
        var auth = facade.register("user1", "pass", "email@example.com");
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutNegativeBadToken() {
        Exception ex = Assertions.assertThrows(Exception.class, () ->
                facade.logout("badToken")
        );
        Assertions.assertTrue(ex.getMessage().toLowerCase().contains("unauthorized")
                || ex.getMessage().toLowerCase().contains("error"));
    }

}