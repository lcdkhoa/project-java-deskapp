package com.expensemanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "expense_manager";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static volatile Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            synchronized (DatabaseConnection.class) {
                if (connection == null || connection.isClosed()) {
                    String host = System.getProperty("db.host", DEFAULT_HOST);
                    String port = System.getProperty("db.port", DEFAULT_PORT);
                    String dbName = System.getProperty("db.name", DEFAULT_DB);
                    String user = System.getProperty("db.user", DEFAULT_USER);
                    String password = System.getProperty("db.password", DEFAULT_PASSWORD);
                    String url = String.format(
                            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                            host, port, dbName);
                    connection = DriverManager.getConnection(url, user, password);
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
