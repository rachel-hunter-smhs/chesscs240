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

    @Test
    void registerNullRegistrationRequest(){
        Assertions.assertThrows(DataAccessException.class,()->userService.register(null));
    }
    @Test
    void registerSameUsername() throws DataAccessException{
        var firstUser = new UserService.RegisterRequest("Spongebob", "BikiniBottom", "squarepants@krustykrab.com");
        userService.register(firstUser);
        var secondUser = new UserService.RegisterRequest("Spongebob", "GaryDaSn@il", "spongebobsquarepants@krustykrab.com");
        Assertions.assertThrows(DataAccessException.class, () -> userService.register(secondUser));
    }

    @Test
    void registerSameEmail() throws DataAccessException {
        var firstUser = new UserService.RegisterRequest("Spongebob", "BikiniBottom", "squarepants@krustykrab.com");
        var result = userService.register(firstUser);
        Assertions.assertEquals("Spongebob", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertTrue(result.authToken().length() > 5);
        var secondUser = new UserService.RegisterRequest("Squidward", "GaryDaSn@il", "squarepants@krustykrab.com");
        var resultTwo= userService.register(secondUser);
        Assertions.assertEquals("Squidward", resultTwo.username());
        Assertions.assertNotNull(resultTwo.authToken());
        Assertions.assertTrue(resultTwo.authToken().length() > 5);

    }
}
