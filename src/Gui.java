import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Gui extends JFrame implements ActionListener {
    //login
    private JButton loginButton;
    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    //showMainApplication() methode
    private DefaultTableModel model;
    private JTable table;
    private JButton showDetailsButton;
    private JButton retourButton;
    private JButton refreshButton;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextArea retourRedenTextArea;
    private JTextField zoekVeld;

    // Verwijzing naar het LoginManager-object voor het afhandelen van inlogoperaties
    private LoginManager loginManager;
    // Verwijzing naar het DatabaseManager-object voor het afhandelen van database-operaties
    private DatabaseManager databaseManager;

    public Gui(LoginManager loginManager) {
        this.loginManager = loginManager;
        this.databaseManager = loginManager.getDatabaseManager();

        initialiseerGui(); //roept de initialiseerGui() methode aan

        pack();
        setLocationRelativeTo(null);
    }

    private void initialiseerGui() {
        setTitle("Retoursysteem");

        loginPanel = new JPanel();
        loginPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        loginPanel.setLayout(new GridLayout(3, 2, 10, 10));
        loginPanel.setBackground(Color.LIGHT_GRAY);

        loginPanel.add(new JLabel("Gebruikersnaam:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Wachtwoord:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        loginButton = new JButton("Inloggen");
        loginPanel.add(loginButton);

        loginButton.addActionListener(this); // voeg actionlistener toe aan loginbutton
        loginPanel.add(loginButton);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(loginPanel, BorderLayout.CENTER);
    }

    private void login() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        if (loginManager.login(username, password)) {
            // Login succesvol
            loginPanel.setVisible(false);
            showMainApplication();
        } else {
            // Login gefaald
            JOptionPane.showMessageDialog(this, "Ongeldige inloggegevens.", "Inloggen mislukt", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMainApplication() {
        model = new DefaultTableModel();
        table = new JTable(model);
        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        showDetailsButton = new JButton("Details");
        showDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSelectedRowDetails();
            }
        });

        retourButton = new JButton("Bestelling refunden");
        retourButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refundOrder();
            }
        });

        retourRedenTextArea = new JTextArea(4, 20);
        retourRedenTextArea.setLineWrap(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(showDetailsButton);
        buttonPanel.add(retourButton);

        JPanel refundPanel = new JPanel();
        refundPanel.setLayout(new BorderLayout());
        refundPanel.add(new JLabel("Refund reden:"), BorderLayout.NORTH);
        refundPanel.add(new JScrollPane(retourRedenTextArea), BorderLayout.CENTER);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        zoekVeld = new JTextField();
        zoekVeld.addActionListener(this);
        searchPanel.add(new JLabel("Zoeken:"), BorderLayout.WEST);
        searchPanel.add(zoekVeld, BorderLayout.CENTER);

        refreshButton = new JButton("Vernieuwen");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(refundPanel, BorderLayout.CENTER);
        bottomPanel.add(searchPanel, BorderLayout.SOUTH);
        bottomPanel.add(refreshButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);


        try {
            //verbinden met database
            Connection connection = databaseManager.getConnection();

            //Statement creeeren
            Statement statement = connection.createStatement();

            //SQL query executen
            String sqlQuery = "SELECT RetourID, BestellingID, StockItemName, Aantal, Reden, extra_info FROM retouren JOIN stockitems ON retouren.StockItemID = stockitems.StockItemID WHERE retouren.Doorgevoerd = 0;";
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            //Metadata van resultset ontvangen
            ResultSetMetaData metaData = resultSet.getMetaData();

            //Aantal collumns opslaan
            int columnCount = metaData.getColumnCount();

            //Array maken voor namen van collumns
            String[] columnNames = new String[columnCount];

            //Columns names krijgen van meta data en deze in table data weergeven
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                columnNames[i - 1] = columnName;
                model.addColumn(columnName);
            }

            //Table vullen met data van result set
            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = resultSet.getObject(i);
                }
                model.addRow(rowData);
            }

            //Resultset, statement en verbinding sluiten
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        pack();
        setLocationRelativeTo(null);
    }

    private void showSelectedRowDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            StringBuilder details = new StringBuilder();
            for (int column = 0; column < model.getColumnCount(); column++) {
                String columnName = model.getColumnName(column);
                Object value = table.getValueAt(selectedRow, column);
                details.append(columnName).append(": ").append(value).append("\n");
            }
            JOptionPane.showMessageDialog(this, details.toString(), "Details", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Geen bestelling geselecteerd", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refundOrder() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int retourID = (int) table.getValueAt(selectedRow, 0); // Veronderstel dat RetourID zich in de eerste kolom bevindt
            String refundReason = retourRedenTextArea.getText();

            if (!refundReason.isEmpty()) {
                // Refund opperation uitvoeren en database updaten

                try {
                    // Verbinden met database
                    Connection connection = databaseManager.getConnection();

                    // Statement voor database updaten
                    String updateQuery = "UPDATE retouren SET Doorgevoerd = 1, Reden_doorgevoerd = ? WHERE RetourID = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);

                    // Parameters toevoegen aan query
                    preparedStatement.setString(1, refundReason);
                    preparedStatement.setInt(2, retourID);

                    // query uitvoeren
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this, "Bestelling succesvol gerefund.", "Refund", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Kon de bestelling niet refunden.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    //Statement en verbinding sluiten
                    preparedStatement.close();
                    connection.close();

                    // Table data refreshen
                    refreshData();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Fout bij het uitvoeren van de update-query.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Geef aub een reden voor refund.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Geen bestelling geselecteerd", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshData() {
        if (model.getRowCount() > 0) {
            return; //Data is al geladen. hoeft niet gerefreshed te worden
        }

        try {
            //Verbinden met database
            Connection connection = databaseManager.getConnection();

            //Statement maken
            Statement statement = connection.createStatement();

            // Query uitvoeren
            String sqlQuery = "SELECT RetourID, BestellingID, StockItemName, Aantal, Reden, extra_info FROM retouren JOIN stockitems ON retouren.StockItemID = stockitems.StockItemID WHERE retouren.Doorgevoerd = 0";
            ResultSet resultSet = statement.executeQuery(sqlQuery);

            //metadata krijgen van resultset
            ResultSetMetaData metaData = resultSet.getMetaData();

            //aantal collommen krijgen
            int columnCount = metaData.getColumnCount();

            // Array maken voor het houden van collom namen
            String[] columnNames = new String[columnCount];

            // Collum namen van meta data stoppen in data table
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                columnNames[i - 1] = columnName;
                model.addColumn(columnName);
            }

            //Table vullen met data van result set
            while (resultSet.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = resultSet.getObject(i);
                }
                model.addRow(rowData);
            }

            // resultSet, Statement en connection sluiten
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            login(); //wanneer de loginbutton word geclicked, word de login() methode aangeroepen
        } else if (e.getSource() == zoekVeld) {
            String searchText = zoekVeld.getText();
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }


}
