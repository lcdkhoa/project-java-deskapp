package com.expensemanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages SQLite database connection. DB file stored in user home / .expense-manager /
 */
public class DatabaseConnection {
    private static final String DB_DIR = ".expense-manager";
    private static final String DB_FILE = "expense.db";
    private static volatile Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            synchronized (DatabaseConnection.class) {
                if (connection == null || connection.isClosed()) {
                    Path dbPath = Paths.get(System.getProperty("user.home"), DB_DIR, DB_FILE);
                    dbPath.getParent().toFile().mkdirs();
                    String url = "jdbc:sqlite:" + dbPath.toString();
                    connection = DriverManager.getConnection(url);
                    connection.setAutoCommit(true);
                }
            }
        }
        return connection;
    }

    public static void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
}
