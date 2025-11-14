package server.dataaccess.mysql;
import server.dataaccess.UserDAO;
import server.dataaccess.DataAccessException;
import server.dataaccess.DatabaseManager;
import model.UserData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class MySqlUserDAO implements UserDAO {

    @Override
    // Broad Summary: make a user in the database
    // Specifics does this by opening a connection, preparing Insert statement, filling in the username
    //password hash, and email and executing that update
    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, passwordHash, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user", e);
        }
    }

    @Override
    //Broad function: get each user's nfo
    // uses SELECT query executes it and returns the desired results
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, passwordHash, email FROM users WHERE username=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"),
                            rs.getString("passwordHash"),
                            rs.getString("email"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user", e);
        }
    }

    @Override
    // Broad funtion: pulls all the users in the database
    //uses a select query to
    public List<UserData> listUsers() throws DataAccessException {
        List<UserData> users = new ArrayList<>();
        String sql = "SELECT username, passwordHash, email FROM users";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new UserData(rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getString("email")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing users", e);
        }
        return users;
    }

    @Override
    public void deleteUser(String username) throws DataAccessException {
        String sql = "DELETE FROM users WHERE username=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting user", e);
        }
    }
    public String hashPassword(String secretPassword){
        if(secretPassword==null||secretPassword.isEmpty()){
            throw new IllegalArgumentException("Password cannot be blank");
        }
        return BCrypt.hashpw(secretPassword,BCrypt.gensalt(12));
    }

    public boolean checkPassword(String secretPassword, String hashedPassword){
        if(secretPassword==null||secretPassword.isEmpty()||hashedPassword==null||hashedPassword.isEmpty()){
            return false;
        }
        try{
            return BCrypt.checkpw(secretPassword,hashedPassword);
        }catch(IllegalArgumentException e){
            return false;
        }
    }
}
