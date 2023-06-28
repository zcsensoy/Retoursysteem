import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url;
    private final String dbUsername;
    private final String dbPassword;

    public DatabaseManager(String url, String username, String password) {
        this.url = url;
        this.dbUsername = username;
        this.dbPassword = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, dbUsername, dbPassword);
    }
}
