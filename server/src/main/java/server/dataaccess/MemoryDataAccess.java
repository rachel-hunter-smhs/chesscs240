package server.dataaccess;

import chess.ChessGame;
import model.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger seq = new AtomicInteger(1);

    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
        seq.set(1);
    }

    @Override
    public void createUser(UserData u) throws DataAccessException {
        if (u == null || u.username() == null || u.password() == null || u.email() == null) {
            throw new DataAccessException("bad request");
        }
        if (users.containsKey(u.username())) {
            throw new DataAccessException("already taken");
        }
        users.put(u.username(), u);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData a) {
        auths.put(a.authToken(), a);
    }

    @Override
    public AuthData getAuth(String token) {
        return auths.get(token);
    }

    @Override
    public void deleteAuth(String token) {
        auths.remove(token);
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isBlank()) {
            throw new DataAccessException("bad request");
        }
        int id = seq.getAndIncrement();
        games.put(id, new GameData(id, null, null, gameName, new ChessGame()));
        return id;
    }

    @Override
    public GameData getGame(int id) {
        return games.get(id);
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public void saveGame(GameData g) throws DataAccessException {
        if (!games.containsKey(g.gameID())) {
            throw new DataAccessException("bad request");
        }
        games.put(g.gameID(), g);
    }
}

