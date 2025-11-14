package server.dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDataAccess implements DataAccess{
    private final Gson gson = new Gson();
    @Override
    public void clear() {
        try (Connection conn = DatabaseManager.getConnection();
        Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth");
            stmt.executeUpdate("DELETE FROM games");
            stmt.executeUpdate("DELETE FROM games");
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database clear failed", e);
        }
    }

    @Override
    public void createUser(UserData u) throws DataAccessException {
        if (u == null || u.username() == null || u.password() == null || u.email() == null) {
            throw new DataAccessException("bad request");
        }
        String checkSql = "SELECT username FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, passwordHash, email) VALUES (?,?,?)";
        try (Connection conn = DatabaseManager.getConnection()){
            try (PreparedStatement check = conn.prepareStatement(checkSql)){
                check.setString(1, u.username());
                try (ResultSet rs = check.executeQuery()){
                    if (rs.next()){
                        throw new DataAccessException("already taken");
                    }
                }
            }
            try(PreparedStatement stmt = conn.prepareStatement(insertSql)){
                stmt.setString(1, u.username());
                stmt.setString(2, u.password());
                stmt.setString(3, u.email());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DataAccessException("Database error",e);
        }
    }

    @Override
    public UserData getUser(String username) {
       String sql = "SELECT username, passwordHash, email FROM users WHERE username = ?";
       try (Connection conn = DatabaseManager.getConnection();
       PreparedStatement stmt = conn.prepareStatement(sql)){
           stmt.setString(1, username);
           try (ResultSet rs = stmt.executeQuery()){
               if (rs.next()){
                   return new UserData(
                           rs.getString("username"),
                           rs.getString("passwordHash"),
                           rs.getString("email"));
               }
               return null;
           }

       }catch (SQLException | DataAccessException e) {
           throw new RuntimeException("Database error", e);
       }
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

}
