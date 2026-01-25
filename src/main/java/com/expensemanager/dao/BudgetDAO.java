package com.expensemanager.dao;

import com.expensemanager.model.Budget;
import com.expensemanager.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for budgets - Section 6.3.4. UNIQUE(user_id, category_id, month_key).
 * 6.4.2 Budget Used per Category: joined query.
 */
public class BudgetDAO {

    /** 6.4.2 Budget Used per Category: category_id, budget, spent, percent_used. */
    public List<BudgetUsedRow> getBudgetUsedPerCategory(Connection conn, String userId, String monthKey) throws SQLException {
        String sql = """
            SELECT b.category_id, b.amount AS budget,
                   ABS(COALESCE(SUM(t.amount), 0)) AS spent,
                   CASE WHEN b.amount > 0 THEN ABS(COALESCE(SUM(t.amount), 0)) * 100.0 / b.amount ELSE 0 END AS percent_used
            FROM budgets b
            LEFT JOIN transactions t ON t.category_id = b.category_id AND t.month_key = b.month_key AND t.type = 'expense'
            WHERE b.user_id = ? AND b.month_key = ?
            GROUP BY b.category_id, b.amount
            """;
        List<BudgetUsedRow> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BudgetUsedRow r = new BudgetUsedRow();
                    r.categoryId = rs.getString("category_id");
                    r.budget = rs.getLong("budget");
                    r.spent = rs.getLong("spent");
                    r.percentUsed = rs.getDouble("percent_used");
                    out.add(r);
                }
            }
        }
        return out;
    }

    public static class BudgetUsedRow {
        public String categoryId;
        public long budget;
        public long spent;
        public double percentUsed;
    }

    public List<Budget> findByUserAndMonth(Connection conn, String userId, String monthKey) throws SQLException {
        String sql = "SELECT id, user_id, category_id, month_key, amount, created_at, updated_at FROM budgets WHERE user_id = ? AND month_key = ? ORDER BY category_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                List<Budget> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public Budget findByUserCategoryMonth(Connection conn, String userId, String categoryId, String monthKey) throws SQLException {
        String sql = "SELECT id, user_id, category_id, month_key, amount, created_at, updated_at FROM budgets WHERE user_id = ? AND category_id = ? AND month_key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, categoryId);
            ps.setString(3, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(Connection conn, Budget b) throws SQLException {
        if (b.getId() == null) b.setId(UUID.randomUUID().toString());
        Instant now = Instant.now();
        if (b.getCreatedAt() == null) b.setCreatedAt(now);
        if (b.getUpdatedAt() == null) b.setUpdatedAt(now);
        String sql = "INSERT INTO budgets (id, user_id, category_id, month_key, amount, created_at, updated_at) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getId());
            ps.setString(2, b.getUserId());
            ps.setString(3, b.getCategoryId());
            ps.setString(4, b.getMonthKey());
            ps.setLong(5, b.getAmount());
            ps.setString(6, b.getCreatedAt().toString());
            ps.setString(7, b.getUpdatedAt().toString());
            ps.executeUpdate();
        }
    }

    public void update(Connection conn, Budget b) throws SQLException {
        b.setUpdatedAt(Instant.now());
        String sql = "UPDATE budgets SET amount = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, b.getAmount());
            ps.setString(2, b.getUpdatedAt().toString());
            ps.setString(3, b.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Connection conn, String id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM budgets WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public long getTotalBudget(Connection conn, String userId, String monthKey) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM budgets WHERE user_id = ? AND month_key = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private static Budget map(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getString("id"));
        b.setUserId(rs.getString("user_id"));
        b.setCategoryId(rs.getString("category_id"));
        b.setMonthKey(rs.getString("month_key"));
        b.setAmount(rs.getLong("amount"));
        String c = rs.getString("created_at");
        if (c != null) b.setCreatedAt(DateUtil.parseInstant(c));
        String u = rs.getString("updated_at");
        if (u != null) b.setUpdatedAt(DateUtil.parseInstant(u));
        return b;
    }
}
