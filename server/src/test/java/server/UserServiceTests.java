package server;

import org.junit.jupiter.api.*;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import service.UserService;

public class UserServiceTests {
    DataAccess dao;
    UserService userService;
    //Creates set up for testing
    @BeforeEach
    void setUp(){
        dao = new MemoryDataAccess();
        userService = new UserService(dao);
    }
    //Checks ability to register a User
    @Test
    void registerPositive() throws DataAccessException{
        var request = new UserService.RegisterRequest("raquelle", "password", "raquelle@hotmail.com");
        var result = userService.register(request);
        Assertions.assertEquals("raquelle", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertTrue(result.authToken().length() > 5);
    }
    //Checks registering a null
    @Test
    void registerNullRegistrationRequest(){
        Assertions.assertThrows(DataAccessException.class,()->userService.register(null));
    }
    //Checks that it can handle two registerees with the same username through errors
    @Test
    void registerSameUsername() throws DataAccessException{
        var firstUser = new UserService.RegisterRequest("Spongebob", "BikiniBottom", "squarepants@krustykrab.com");
        userService.register(firstUser);
        var secondUser = new UserService.RegisterRequest("Spongebob", "GaryDaSn@il", "spongebobsquarepants@krustykrab.com");
        Assertions.assertThrows(DataAccessException.class, () -> userService.register(secondUser));
    }
    //Tries to users with same email
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
    //Checks if login is possible with correct information
    @Test
    void loginPositive() throws DataAccessException {
        var firstUser = new UserService.RegisterRequest("Sherlock", "A5tudyInSc@rlet", "sherlockHolmes@gmail.com");
        userService.register(firstUser);
        var loginAttempt = new UserService.LoginRequest("Sherlock", "A5tudyInSc@rlet");
        var result = userService.login(loginAttempt);
        Assertions.assertEquals("Sherlock", result.username());
        Assertions.assertNotNull(result.authToken());
    }
    //Test for login that has incorrect password
    @Test
    void loginWrongPassword() throws DataAccessException {
        var firstUser = new UserService.RegisterRequest("Sherlock", "A5tudyInSc@rlet", "sherlockHolmes@gmail.com");
        userService.register(firstUser);
        var loginAttempt = new UserService.LoginRequest("Sherlock", "NotRightPassword");
        Assertions.assertThrows(DataAccessException.class, () -> userService.login(loginAttempt));
    }
    //Login test for user not in database
    @Test
    void loginNonexistingUser(){
        var loginAttempt = new UserService.LoginRequest("Sherlock", "NotRightPassword");
        Assertions.assertThrows(DataAccessException.class, () -> userService.login(loginAttempt));
    }

}
