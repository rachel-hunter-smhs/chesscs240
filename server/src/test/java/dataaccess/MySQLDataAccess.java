package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

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
            stmt.executeUpdate("DELETE FROM users");
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
            String hashed = BCrypt.hashpw(u.password(), BCrypt.gensalt());

            try(PreparedStatement stmt = conn.prepareStatement(insertSql)){
                stmt.setString(1, u.username());
                stmt.setString(2, hashed);
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
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1,a.authToken());
            stmt.setString(2, a.username());
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e){
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public AuthData getAuth(String token) {
        String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,token);
            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username"));

                }
                return null;
            }

        } catch (SQLException | DataAccessException e){
            throw new RuntimeException("Database error", e);
        }

    }

    @Override
    public void deleteAuth(String token) {
        String sql = "DELETE FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt =conn.prepareStatement(sql)){
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e){
            throw new RuntimeException("Database error", e);
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isBlank()) {
            throw new DataAccessException("bad request");
        }
       String sql = "INSERT INTO games (gameName, whiteUsername, blackUsername, gameState) VALUES (?, ?, ?, ?)";
        ChessGame game = new ChessGame();
        String gameStateJson = gson.toJson(game);

        try(Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, gameName);
            stmt.setNull(2, Types.VARCHAR);
            stmt.setNull(3,Types.VARCHAR);
            stmt.setString(4, gameStateJson);
            stmt.executeUpdate();

            try(ResultSet keys = stmt.getGeneratedKeys()){
                if(keys.next()){
                    return keys.getInt(1);
                } else{
                    throw new DataAccessException("Database error");
                }
            }
        } catch (SQLException e){
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public GameData getGame(int id) {
       String sql = "SELECT id, gameName, whiteUsername, blackUsername, gameState FROM games WHERE id = ?";
       try (Connection conn = DatabaseManager.getConnection();
       PreparedStatement stmt = conn.prepareStatement(sql)){
           stmt.setInt(1, id);
           try (ResultSet rs = stmt.executeQuery()){
               if(!rs.next()){
                   return  null;
               }
               String gameStateJson = rs.getString("gameState");
               ChessGame game;
               try {
                   game = gson.fromJson(gameStateJson, ChessGame.class);
                   if (game == null){
                       game = new ChessGame();
                   }
               } catch (Exception e){
                   game = new ChessGame();
               }
               return new GameData(
                       rs.getInt("id"),
                       rs.getString("whiteUsername"),
                       rs.getString("blackUsername"),
                       rs.getString("gameName"),
                       game);

           }
       } catch (SQLException | DataAccessException e){
           throw  new RuntimeException("Database error", e);
       }
    }

    @Override
    public List<GameData> listGames() {
       String sql = "SELECT id, gameName, whiteUsername, blackUsername, gameState FROM games";
       List<GameData> result = new ArrayList<>();
       try (Connection conn = DatabaseManager.getConnection();
       PreparedStatement stmt = conn.prepareStatement(sql);
       ResultSet rs = stmt.executeQuery()){

           while (rs.next()){
               String gameStateJson = rs.getString("gameState");
               ChessGame game;
               try{
                   game = gson.fromJson(gameStateJson, ChessGame.class);
                   if (game == null){
                       game = new ChessGame();
                   }
               } catch (Exception e){
                   game = new ChessGame();
               }
               result.add(new GameData(
                       rs.getInt("id"),
                       rs.getString("whiteUsername"),
                       rs.getString("blackUsername"),
                       rs.getString("gameName"),
                       game));
           }
           return result;
       } catch (SQLException | DataAccessException e){
           throw new RuntimeException("Database error", e);
       }
    }

    @Override
    public void saveGame(GameData g) throws DataAccessException {
       String sql =  "UPDATE games SET gameName = ?, whiteUsername = ?, blackUsername = ?, gameState = ? WHERE id = ?";
       String gameStateJson = gson.toJson(g.game());

       try (Connection conn = DatabaseManager.getConnection();
       PreparedStatement stmt = conn.prepareStatement(sql)){
           stmt.setString(1,g.gameName());
           stmt.setString(2,g.whiteUsername());
           stmt.setString(3,g.blackUsername());
           stmt.setString(4,gameStateJson);
           stmt.setInt(5,g.gameID());
           int updated = stmt.executeUpdate();
           if (updated == 0){
               throw new DataAccessException("bad request");
           }
       } catch (SQLException e){
           throw new DataAccessException("Database error", e);
       }
    }
}


