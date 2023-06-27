import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginManager {
    private DatabaseManager databaseManager;

    public LoginManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public boolean login(String username, String password) {

        try {
            Connection connection = databaseManager.getConnection();

            String loginQuery = "SELECT * FROM Medewerker WHERE Gebruikersnaam = ? AND Wachtwoord = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(loginQuery);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            boolean loginSuccessful = resultSet.next();

            resultSet.close();
            preparedStatement.close();
            connection.close();

            return loginSuccessful;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
