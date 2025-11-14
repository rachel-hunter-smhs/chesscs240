package server.service;

import server.dataaccess.DataAccess;
import server.dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final DataAccess dao;
    public UserService(DataAccess d){ dao = d; }

    public record RegisterRequest(String username,String password,String email){}
    public record RegisterResult(String username,String authToken){}
    public RegisterResult register(RegisterRequest r) throws DataAccessException {
        if(r==null||r.username()==null||r.password()==null||r.email()==null) throw new DataAccessException("bad request");
        dao.createUser(new UserData(r.username(),r.password(),r.email()));
        String t = UUID.randomUUID().toString();
        dao.createAuth(new AuthData(t,r.username()));
        return new RegisterResult(r.username(),t);
    }

    public record LoginRequest(String username,String password){}
    public record LoginResult(String username,String authToken){}
    public LoginResult login(LoginRequest r) throws DataAccessException {
        if(r==null||r.username()==null||r.password()==null) throw new DataAccessException("bad request");
        var u = dao.getUser(r.username());
        if(u==null||!BCrypt.checkpw(r.password(),u.password())) throw new DataAccessException("unauthorized");
        String t = UUID.randomUUID().toString();
        dao.createAuth(new AuthData(t,u.username()));
        return new LoginResult(u.username(),t);
    }

    public record LogoutRequest(String authToken){}
    public void logout(LogoutRequest r) throws DataAccessException {
        if(r==null||r.authToken()==null) throw new DataAccessException("unauthorized");
        var a = dao.getAuth(r.authToken());
        if(a==null) throw new DataAccessException("unauthorized");
        dao.deleteAuth(r.authToken());
    }
}
