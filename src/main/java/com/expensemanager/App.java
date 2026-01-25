package com.expensemanager;

import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.db.Schema;
import com.expensemanager.db.SeedData;
import com.expensemanager.view.MainFrame;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Entry point. Init DB (schema, seed, default user), set FlatLaf, show MainFrame.
 */
public class App {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Schema.createSchema(conn);
            SeedData.seedCategories(conn);
            String userId = SeedData.ensureDefaultUser(conn);
            AppContext.setUserId(userId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database init failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        FlatLaf.setup();
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
