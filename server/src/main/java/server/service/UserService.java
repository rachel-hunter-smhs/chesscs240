package server.service;
import server.dataaccess.DataAccess;
import server.dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final DataAccess dao;
    public UserService (DataAccess d){ dao = d;}

    public record RegisterRequest(String username,String password,String email){}
    public record RegisterResult
}
