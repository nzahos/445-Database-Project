package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.sql.*;

/**
 * This class is responsible for handling the following:
 * Check if (when and where/date and LibraryID) there are any upcoming shipments of a specified book name.
 */
public class CheckShipmentsPage {
    private static JFrame myFrame;
    private JTextField nameField;
    private JTextArea resultArea;

    public CheckShipmentsPage() {
        myFrame = new JFrame("Check for Upcoming Shipments");
        myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        myFrame.setSize(600, 400);

        JPanel myPanel = new JPanel(new GridLayout(6, 2));
        myPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        myPanel.add(new JLabel("Book Name: "));
        nameField = new JTextField();
        myPanel.add(nameField);

        JButton checkButton = new JButton("Check For Book's Shipments");
        myPanel.add(checkButton);
        checkButton.addActionListener(e -> {
            String bookName = nameField.getText();

            ResultSet result = null;
            if (!bookName.isEmpty()) {
                result = getUpcomingShipmentsOfBookName(bookName);
            }

            // Call a function to display the result in resultArea
            displayResult(result);
        });

        // Back button to get back to the main page.
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        myFrame.add(myPanel, BorderLayout.NORTH);
        myFrame.add(scrollPane, BorderLayout.CENTER);
        myFrame.add(backButton, BorderLayout.SOUTH);

        myFrame.setLocationRelativeTo(null);
        myFrame.setVisible(true);
    }

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

    public ResultSet getUpcomingShipmentsOfBookName(String bookName) {
        String query = "SELECT BOOK.BookName as 'Book Name', LIBRARY.LibraryID as 'Library ID', " +
        "AVAILABILITY.Quantity as 'Available Quantity' " +
        "FROM BOOK " +
        "INNER JOIN AVAILABILITY ON BOOK.BookID = AVAILABILITY.BookID " +
        "INNER JOIN LIBRARY ON AVAILABILITY.LibraryID = LIBRARY.LibraryID " +
        "INNER JOIN PUBLISHER ON BOOK.PublisherID = PUBLISHER.PublisherID " +
        "WHERE BOOK.BookName = ? AND AVAILABILITY.LibraryID = ? " +
        "AND CONVERT(DATE, AVAILABILITY.ShipDate, 101) >= CONVERT(DATE, GETDATE(), 101)";
        try {
            PreparedStatement stmt = getConnection().prepareStatement(query);
            stmt.setString(1, bookName);
            return stmt.executeQuery();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void displayResult(ResultSet result) {
        StringBuilder resultText = new StringBuilder("Results:\n");
        try {
            while (result != null && result.next()) {
                String shipmentDate = result.getString("ShipmentDate");
                String libraryID = result.getString("LibraryID");
                String cityID = result.getString("CityID");
                String stateID = result.getString("StateID");
                String zipCode = result.getString("ZipCode");

                String line = "Shipment Date: " + shipmentDate + "\n";
                line += "Library ID: " + libraryID + "\n";
                line += "City ID: " + cityID + "\n";
                line += "State ID: " + stateID + "\n";
                line += "Zip Code: " + zipCode + "\n";

                resultText.append(line).append("\n");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        resultArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        resultArea.setText(resultText.toString());
    }
}
