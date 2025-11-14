package dataaccess;

import model.*;
import java.util.List;

public interface DataAccess {
    void clear();

    void createUser(UserData u) throws DataAccessException;
    UserData getUser(String username);

    void createAuth(AuthData a);
    AuthData getAuth(String token);
    void deleteAuth(String token);

    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int id);
    List<GameData> listGames();
    void saveGame(GameData g) throws DataAccessException;
}
