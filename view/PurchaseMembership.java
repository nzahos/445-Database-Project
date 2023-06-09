package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.sql.*;
import java.time.LocalDate;

/**
 * This class is responsible for handling the following:
 * Purchase a membership with this library franchise.
 */
public class PurchaseMembership {
    private static JFrame myFrame;
    private JTextField nameField;
    private JTextArea resultArea;

    /**
     * Initializes the PurchaseMembership class by creating the GUI frame and components.
     */
    public PurchaseMembership() {
        // Create the main frame
        myFrame = new JFrame("Purchase Library Membership");
        myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myFrame.setSize(600, 400);

        // Create the panel and set the layout
        JPanel myPanel = new JPanel(new GridLayout(6, 2));
        myPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create and add a label and text field for customer name input
        myPanel.add(new JLabel("Customer Name: "));
        nameField = new JTextField();
        myPanel.add(nameField);

        // Create and add a button for purchasing membership
        JButton purchaseButton = new JButton("Purchase Membership");
        myPanel.add(purchaseButton);
        purchaseButton.addActionListener(e -> {
            // Get the customer name entered by the user
            String customerName = nameField.getText();

            ResultSet result = null;
            if (!customerName.isEmpty()) {
                // Call the purchaseMembership method to purchase the membership
                result = purchaseMembership(customerName);
            }

            // Call a function to display the result in resultArea
            displayResult(result);
        });

        // Create a back button to get back to the main page
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Close the current frame and make the main page frame visible
                myFrame.dispose();
                MainPage.myFrame.setVisible(true);
            }
        });

        // Make sure that if the user clicks the 'X', the main page is restored
        myFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MainPage.myFrame.setVisible(true);
            }
        });

        // Create a text area for displaying the result
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Add the components to the frame
        myFrame.add(myPanel, BorderLayout.NORTH);
        myFrame.add(scrollPane, BorderLayout.CENTER);
        myFrame.add(backButton, BorderLayout.SOUTH);

        myFrame.setLocationRelativeTo(null);
        myFrame.setVisible(true);
    }

    /**
     * Establishes a connection to the database.
     *
     * @return the database connection
     */
    public Connection getConnection() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=LibraryDB;integratedSecurity=true;trustServerCertificate=true;";
            Connection conn = DriverManager.getConnection(url);
            return conn;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Purchases a membership for a customer.
     *
     * @param customerName the name of the customer
     * @return the ResultSet containing the updated customer information, or null if the customer name does not exist
     */
    public ResultSet purchaseMembership(String customerName) {
        try {
            // Check if the customer name exists in the database
            String checkQuery = "SELECT * FROM CUSTOMER WHERE CustomerName = ?";
            PreparedStatement checkStmt = getConnection().prepareStatement(checkQuery);
            checkStmt.setString(1, customerName);
            ResultSet checkResult = checkStmt.executeQuery();
            if (!checkResult.next()) {
                return null; // Customer name does not exist in the database
            }

            // Purchase a new membership
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusMonths(1);

            String purchaseQuery = "DECLARE @NewMembershipID INT;" +
                    "BEGIN TRANSACTION;" +
                    "INSERT INTO MEMBERSHIP(StartDate, EndDate) VALUES (?, ?);" +
                    "SET @NewMembershipID = SCOPE_IDENTITY();" +
                    "UPDATE CUSTOMER SET MembershipID = @NewMembershipID WHERE CustomerName = ?;" +
                    "COMMIT;";
            PreparedStatement purchaseStmt = getConnection().prepareStatement(purchaseQuery);
            purchaseStmt.setString(1, startDate.toString());
            purchaseStmt.setString(2, endDate.toString());
            purchaseStmt.setString(3, customerName);
            purchaseStmt.executeUpdate();

            // Retrieve the updated customer information
            String retrieveQuery = "SELECT * FROM CUSTOMER WHERE CustomerName = ?";
            PreparedStatement retrieveStmt = getConnection().prepareStatement(retrieveQuery);
            retrieveStmt.setString(1, customerName);
            return retrieveStmt.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Displays the result in the resultArea.
     *
     * @param result the ResultSet containing the result to display
     */
    public void displayResult(ResultSet result) {
        StringBuilder resultText = new StringBuilder("Results:\n");
        try {
            if (result == null) {
                resultText.append("This customer name does not exist in the database.");
            } else {
                resultText.append("PURCHASE SUCCESSFUL! Here is your membership info:\n");
                while (result.next()) {
                    int customerID = result.getInt("CustomerID");
                    String customerName = result.getString("CustomerName");
                    int membershipID = result.getInt("MembershipID");

                    String line = "Customer ID: " + customerID + "\n";
                    line += "Customer Name: " + customerName + "\n";
                    line += "Membership ID: " + membershipID + "\n";

                    resultText.append(line).append("\n");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        resultArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        resultArea.setText(resultText.toString());
    }
}

