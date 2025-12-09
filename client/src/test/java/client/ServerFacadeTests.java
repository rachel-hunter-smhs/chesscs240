package client;

import model.AuthData;
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
    public void clearDatabase () throws Exception {
        facade.clear();
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
        Assertions.assertThrows(Exception.class, () ->
                facade.register("user1", "pass", "email@example.com")
        );
    }
    @Test
    void loginNegativeBadPassword() throws Exception {
        facade.register("user1", "pass", "email@example.com");
        Assertions.assertThrows(Exception.class, () ->
                facade.login("user1", "wrong")
        );
    }

    @Test
    void logoutPositive() throws Exception {
        var auth = facade.register("user1", "pass", "email@example.com");
        Assertions.assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutNegativeBadToken() {
        Assertions.assertThrows(Exception.class, () ->
                facade.logout("badToken")
        );
    }

    @Test
    void createGamePositive() throws Exception{
        AuthData auth = facade.register("user1","pass", "email@example.com");
        var response = facade.createGame(auth.authToken(), "TestGame");
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.gameID()>0);
    }
    @Test
    void createGameNegativeNoAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.createGame("badToken", "TestGame")
        );
    }
    @Test
    void listGamePositive() throws Exception{
        AuthData auth = facade.register("user1", "pass", "email@ex.com");
        facade.createGame(auth.authToken(), "Game1");
        facade.createGame(auth.authToken(), "Game2");

        var test = facade.listGames(auth.authToken());
        Assertions.assertNotNull(test);
        Assertions.assertTrue(test.games().length >=2);
    }
    @Test
    void listGamesNegNoAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.listGames("badToken")
        );
    }
    @Test
    void joinGamePos() throws Exception{
        AuthData auth = facade.register("user1", "PaSs", "email@ex.com");
        var gamePlay = facade.createGame(auth.authToken(), "TestGame");

        Assertions.assertDoesNotThrow(() ->
                facade.joinGame(auth.authToken(), gamePlay.gameID(), "WHITE"));

    }
    @Test
    void joinGameNegBadGameID() throws Exception{
        AuthData auth = facade.register("user1", "pass", "email@ex.com");
        Exception except = Assertions.assertThrows(Exception.class,
                ()-> facade.joinGame(auth.authToken(), 9999, "WHITE"));
        Assertions.assertNotNull(except.getMessage());
    }

}