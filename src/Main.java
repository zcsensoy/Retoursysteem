
public class Main {
    public static void main(String[] args)  {
        DatabaseManager databaseManager = new DatabaseManager("jdbc:mysql://localhost:3306/nerdygadgets", "root", "");
        Gui gui = new Gui(new LoginManager(databaseManager));

        gui.setVisible(true);
    }


}