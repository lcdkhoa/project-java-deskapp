package com.expensemanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseConnection {
    private static final String DB_FILE = "expense.db";
    private static volatile Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            synchronized (DatabaseConnection.class) {
                if (connection == null || connection.isClosed()) {
                    Path dbPath = resolveAppDbPath();
                    ensureDbDirectory(dbPath);
                    String url = "jdbc:sqlite:" + dbPath.toString();
                    connection = DriverManager.getConnection(url);
                    connection.setAutoCommit(true);
                    ensureSchema(connection);
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

    private static Path resolveAppDbPath() {
        // Prefer Windows %APPDATA%, fallback to user.home on other OS.
        String appData = System.getenv("APPDATA");
        Path baseDir = (appData != null && !appData.isBlank())
                ? Paths.get(appData, "PersonalExpenseManager")
                : Paths.get(System.getProperty("user.home"), ".personal-expense-manager");
        return baseDir.resolve(DB_FILE);
    }

    private static void ensureDbDirectory(Path dbPath) throws SQLException {
        try {
            Files.createDirectories(dbPath.getParent());
        } catch (Exception e) {
            throw new SQLException("Failed to prepare local database at: " + dbPath, e);
        }
    }

    /**
     * Ensure that required tables exist. If not, run init.sql from classpath.
     */
    private static void ensureSchema(Connection conn) throws SQLException {
        // Quick check: if categories table exists, assume schema is present.
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name='categories'")) {
            if (rs.next()) {
                return;
            }
        }

        // Run init.sql from resources
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/com/expensemanager/db/init.sql")) {
            if (in == null) {
                throw new SQLException("DB init script not found in resources: /com/expensemanager/db/init.sql");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip comment lines
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    sb.append(line).append('\n');
                }
                String[] statements = sb.toString().split(";");
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : statements) {
                        String s = sql.trim();
                        if (s.isEmpty()) {
                            continue;
                        }
                        stmt.executeUpdate(s);
                    }
                }
            }
        } catch (Exception e) {
            throw new SQLException("Failed to initialize database schema from init.sql", e);
        }
    }
}
