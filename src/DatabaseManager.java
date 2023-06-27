import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private String url;
    private String dbUsername;
    private String dbPassword;

    public DatabaseManager(String url, String username, String password) {
        this.url = "jdbc:mysql://localhost:3306/nerdygadgets";
        this.dbUsername = "root";
        this.dbPassword = "";
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, dbUsername, dbPassword);
    }
}
