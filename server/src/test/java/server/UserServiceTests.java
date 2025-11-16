package server;

import org.junit.jupiter.api.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import service.UserService;

public class UserServiceTests {
    DataAccess dao;
    UserService userService;

    @BeforeEach
    void setUp(){
        dao = new MemoryDataAccess();
        userService = new UserService(dao);
    }

    @Test
    void registerPositive() throws DataAccessException{
        var request = new UserService.RegisterRequest("raquelle", "password", "raquelle@hotmail.com");
        var result = userService.register(request);
        Assertions.assertEquals("raquelle", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertTrue(result.authToken().length() > 5);
    }
}
