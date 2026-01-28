package com.expensemanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseConnection {
    private static final String DB_DIR = "src/main/java/com/expensemanager/db";
    private static final String DB_FILE = "expense.db";
    private static volatile Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            synchronized (DatabaseConnection.class) {
                if (connection == null || connection.isClosed()) {
                    Path dbPath = Paths.get(DB_DIR, DB_FILE);
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
