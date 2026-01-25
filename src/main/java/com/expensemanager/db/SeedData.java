package com.expensemanager.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * Seeds categories from Section 6.5.2 (Expense) and 6.5.3 (Income).
 * Idempotent: only inserts if table is empty for categories.
 */
public class SeedData {

    private static final String[][] EXPENSE_CATEGORIES = {
        {"Food", "🍽", "#4F46E5"},
        {"Transport", "🚗", "#6366F1"},
        {"Housing", "🏠", "#8B5CF6"},
        {"Bills", "📄", "#7C3AED"},
        {"Shopping", "🛍", "#EC4899"},
        {"Entertainment", "🎬", "#F59E0B"},
        {"Coffee", "☕", "#A16207"},
        {"Healthcare", "💊", "#22C55E"},
        {"Education", "🎓", "#0EA5E9"},
        {"Other", "📦", "#6B7280"}
    };

    private static final String[][] INCOME_CATEGORIES = {
        {"Salary", "💼", "#22C55E"},
        {"Freelance", "🧑‍💻", "#10B981"},
        {"Affiliate", "🔗", "#14B8A6"},
        {"Selling", "💸", "#16A34A"},
        {"Other Income", "➕", "#4ADE80"}
    };

    public static void seedCategories(Connection conn) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM categories");
             ResultSet rs = check.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already seeded
            }
        }

        String ins = "INSERT INTO categories (id, name, icon, color, type, is_active, created_at) VALUES (?, ?, ?, ?, ?, 1, ?)";
        String ts = Instant.now().toString();

        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            for (String[] row : EXPENSE_CATEGORIES) {
                ps.setString(1, java.util.UUID.randomUUID().toString());
                ps.setString(2, row[0]);
                ps.setString(3, row[1]);
                ps.setString(4, row[2]);
                ps.setString(5, "expense");
                ps.setString(6, ts);
                ps.executeUpdate();
            }
            for (String[] row : INCOME_CATEGORIES) {
                ps.setString(1, java.util.UUID.randomUUID().toString());
                ps.setString(2, row[0]);
                ps.setString(3, row[1]);
                ps.setString(4, row[2]);
                ps.setString(5, "income");
                ps.setString(6, ts);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Ensures a default user exists (single-user). Returns user id.
     */
    public static String ensureDefaultUser(Connection conn) throws SQLException {
        try (PreparedStatement sel = conn.prepareStatement("SELECT id FROM users LIMIT 1");
             ResultSet rs = sel.executeQuery()) {
            if (rs.next()) {
                return rs.getString("id");
            }
        }
        String id = java.util.UUID.randomUUID().toString();
        String ts = Instant.now().toString();
        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO users (id, email, created_at) VALUES (?, ?, ?)")) {
            ins.setString(1, id);
            ins.setString(2, "default@local");
            ins.setString(3, ts);
            ins.executeUpdate();
        }
        return id;
    }
}
