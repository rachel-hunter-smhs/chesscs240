package server.dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import server.dataaccess.DataAccessException;

/** Loads db.properties, creates DB and tables when enabled, provides connections. */
public final class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;
    private static boolean autoCreate;

    static {
        loadPropertiesFromResources();
        if (autoCreate) {
            try { Schema.init(); }
            catch (DataAccessException e) { throw new RuntimeException(e); }
        }
    }

    /** Creates the database catalog if it does not exist. */
    public static void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    /** Returns a connection with the catalog set. Close it with try-with-resources. */
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) throw new RuntimeException("db.properties missing");
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");
        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        autoCreate = Boolean.parseBoolean(props.getProperty("db.autoCreate", "true"));
        connectionUrl = String.format(
                "jdbc:mysql://%s:%d?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host, port);
    }

    /** One shot schema initializer. */
    public static final class Schema {
        public static void init() throws DataAccessException {
            createDatabase();
            try (Connection c = getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users(
                      username VARCHAR(64) PRIMARY KEY,
                      passwordHash VARCHAR(100) NOT NULL,
                      email VARCHAR(255) NOT NULL
                    ) ENGINE=InnoDB
                """);
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS auth(
                      authToken CHAR(36) PRIMARY KEY,
                      username VARCHAR(64) NOT NULL,
                      createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT fk_auth_user FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
                    ) ENGINE=InnoDB
                """);
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS games(
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      gameName VARCHAR(255) NOT NULL,
                      whiteUsername VARCHAR(64),
                      blackUsername VARCHAR(64),
                      gameState TEXT NOT NULL,
                      createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT fk_white_user FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
                      CONSTRAINT fk_black_user FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
                    ) ENGINE=InnoDB
                """);
            } catch (Exception e) {
                throw new DataAccessException("schema init failed", e);
            }
        }
    }
}
