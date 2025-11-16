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
}
