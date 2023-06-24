import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

public class Gui extends JFrame implements ActionListener {
    private JTable table;
    private DefaultTableModel model;
    private JButton showDetailsButton;
    private JButton retourButton;
    private JTextArea retourRedenTextArea;
    private JTextField zoekVeld;
    private JButton refreshButton;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public Gui() {
        setTitle("Retoursysteem");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Gebruikersnaam:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Wachtwoord:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);
        JButton loginButton = new JButton("Inloggen");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        loginPanel.add(loginButton);

        add(loginPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        // Database connectie details
        String url = "jdbc:mysql://localhost:3306/nerdygadgets";
        String dbUsername = "root";
        String dbPassword = "";

        try {
            //Verbinden met database
            Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);

            //Statement preparen voor login query
            String loginQuery = "SELECT * FROM Medewerker WHERE Gebruikersnaam = ? AND Wachtwoord = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(loginQuery);

            //Values voor username en password toewijzen aan query
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            //Login query executen
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Login successful
                loginPanel.setVisible(false);
                showMainApplication();
            } else {
                // Login failed
                JOptionPane.showMessageDialog(this, "Ongeldige inloggegevens.", "Inloggen mislukt", JOptionPane.ERROR_MESSAGE);
            }

            //result set, prepared statement en connectie sluiten
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fout bij het uitvoeren van de login-query.", "Error", JOptionPane.ERROR_MESSAGE);
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

        // Database connectie details
        String url = "jdbc:mysql://localhost:3306/nerdygadgets";
        String username = "root";
        String password = "";

        try {
            //verbinden met database
            Connection connection = DriverManager.getConnection(url, username, password);

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
                String url = "jdbc:mysql://localhost:3306/nerdygadgets";
                String username = "root";
                String password = "";

                try {
                    // Verbinden met database
                    Connection connection = DriverManager.getConnection(url, username, password);

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

        // Database verbinding details
        String url = "jdbc:mysql://localhost:3306/nerdygadgets";
        String username = "root";
        String password = "";

        try {
            //Verbinden met database
            Connection connection = DriverManager.getConnection(url, username, password);

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

    public void actionPerformed(ActionEvent event) {
        String text = zoekVeld.getText();
        if (text.trim().length() == 0) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Gui gui = new Gui();
                gui.setVisible(true);
            }
        });
    }
}