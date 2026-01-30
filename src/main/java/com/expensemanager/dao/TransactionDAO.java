package com.expensemanager.dao;

import com.expensemanager.model.Transaction;
import com.expensemanager.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransactionDAO {

    public long getMonthlyExpense(Connection conn, String monthKey) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE month_key = ? AND type = 'expense'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    public long getMonthlyIncome(Connection conn, String monthKey) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE month_key = ? AND type = 'income'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    public Map<LocalDate, Long> getCashflowByDay(Connection conn, String monthKey) throws SQLException {
        String sql = "SELECT transaction_date, SUM(amount) AS cashflow FROM transactions WHERE month_key = ? GROUP BY transaction_date ORDER BY transaction_date";
        Map<LocalDate, Long> out = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(LocalDate.parse(rs.getString("transaction_date")), rs.getLong("cashflow"));
                }
            }
        }
        return out;
    }

    public Map<LocalDate, Long> getIncomeByDay(Connection conn, String monthKey) throws SQLException {
        String sql = "SELECT transaction_date, SUM(amount) AS total FROM transactions WHERE month_key = ? AND type = 'income' GROUP BY transaction_date ORDER BY transaction_date";
        Map<LocalDate, Long> out = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(LocalDate.parse(rs.getString("transaction_date")), rs.getLong("total"));
                }
            }
        }
        return out;
    }

    public Map<LocalDate, Long> getExpenseByDay(Connection conn, String monthKey) throws SQLException {
        String sql = "SELECT transaction_date, ABS(SUM(amount)) AS total FROM transactions WHERE month_key = ? AND type = 'expense' GROUP BY transaction_date ORDER BY transaction_date";
        Map<LocalDate, Long> out = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(LocalDate.parse(rs.getString("transaction_date")), rs.getLong("total"));
                }
            }
        }
        return out;
    }

    public Map<Integer, Long> getSpendingByWeekday(Connection conn) throws SQLException {
        String sql = "SELECT (DAYOFWEEK(STR_TO_DATE(transaction_date, '%Y-%m-%d')) - 1) AS weekday, ABS(SUM(amount)) AS total FROM transactions WHERE type = 'expense' GROUP BY weekday";
        Map<Integer, Long> out = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getInt("weekday"), rs.getLong("total"));
                }
            }
        }
        return out;
    }

    public Map<String, Long> getExpenseByCategory(Connection conn, String monthKey) throws SQLException {
        String sql = "SELECT category_id, ABS(SUM(amount)) AS total FROM transactions WHERE month_key = ? AND type = 'expense' GROUP BY category_id HAVING total > 0";
        Map<String, Long> out = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getString("category_id"), rs.getLong("total"));
                }
            }
        }
        return out;
    }

    public Map<LocalDate, Long> getExpenseByDateRange(Connection conn, LocalDate start, LocalDate end)
            throws SQLException {
        String sql = "SELECT transaction_date, ABS(SUM(amount)) AS total FROM transactions WHERE type = 'expense' AND transaction_date >= ? AND transaction_date <= ? GROUP BY transaction_date ORDER BY transaction_date";
        Map<LocalDate, Long> out = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(LocalDate.parse(rs.getString("transaction_date")), rs.getLong("total"));
                }
            }
        }
        return out;
    }

    public void insert(Connection conn, Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (id, amount, type, category_id, wallet_type, note, transaction_date, transaction_time, month_key, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        if (t.getId() == null)
            t.setId(UUID.randomUUID().toString());
        Instant now = Instant.now();
        if (t.getCreatedAt() == null)
            t.setCreatedAt(now);
        if (t.getUpdatedAt() == null)
            t.setUpdatedAt(now);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getId());
            ps.setLong(2, t.getAmount());
            ps.setString(3, t.getType());
            ps.setString(4, t.getCategoryId());
            ps.setString(5, t.getWalletType());
            ps.setString(6, t.getNote());
            ps.setString(7, t.getTransactionDate().toString());
            ps.setString(8, t.getTransactionTime().toString());
            ps.setString(9, t.getMonthKey());
            ps.setString(10, t.getCreatedAt().toString());
            ps.setString(11, t.getUpdatedAt().toString());
            ps.executeUpdate();
        }
    }

    public void update(Connection conn, Transaction t) throws SQLException {
        t.setUpdatedAt(Instant.now());
        String sql = "UPDATE transactions SET amount=?, type=?, category_id=?, wallet_type=?, note=?, transaction_date=?, transaction_time=?, month_key=?, updated_at=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, t.getAmount());
            ps.setString(2, t.getType());
            ps.setString(3, t.getCategoryId());
            ps.setString(4, t.getWalletType());
            ps.setString(5, t.getNote());
            ps.setString(6, t.getTransactionDate().toString());
            ps.setString(7, t.getTransactionTime().toString());
            ps.setString(8, t.getMonthKey());
            ps.setString(9, t.getUpdatedAt().toString());
            ps.setString(10, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Connection conn, String id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public Transaction findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, amount, type, category_id, wallet_type, note, transaction_date, transaction_time, month_key, created_at, updated_at FROM transactions WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public List<Transaction> search(Connection conn, String categoryId, String walletType,
            LocalDate startDate, LocalDate endDate, String sortBy, String searchNote) throws SQLException {
        List<String> cond = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        if (categoryId != null && !categoryId.isEmpty()) {
            cond.add("category_id = ?");
            args.add(categoryId);
        }
        if (walletType != null && !walletType.isEmpty()) {
            cond.add("wallet_type = ?");
            args.add(walletType);
        }
        if (startDate != null) {
            cond.add("transaction_date >= ?");
            args.add(startDate.toString());
        }
        if (endDate != null) {
            cond.add("transaction_date <= ?");
            args.add(endDate.toString());
        }
        if (searchNote != null && !searchNote.isBlank()) {
            cond.add("COALESCE(note,'') LIKE ?");
            args.add("%" + searchNote.trim() + "%");
        }
        String order;
        if ("date_asc".equals(sortBy)) {
            order = "transaction_date ASC, transaction_time ASC";
        } else if ("amount_desc".equals(sortBy)) {
            order = "ABS(amount) DESC, transaction_date DESC, transaction_time DESC";
        } else if ("amount_asc".equals(sortBy)) {
            order = "ABS(amount) ASC, transaction_date DESC, transaction_time DESC";
        } else {
            order = "transaction_date DESC, transaction_time DESC";
        }

        String whereClause = cond.isEmpty() ? "" : "WHERE " + String.join(" AND ", cond);
        String sql = "SELECT id, amount, type, category_id, wallet_type, note, transaction_date, transaction_time, month_key, created_at, updated_at FROM transactions "
                + whereClause + " ORDER BY " + order;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.size(); i++) {
                ps.setString(i + 1, String.valueOf(args.get(i)));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Transaction> list = new ArrayList<>();
                while (rs.next())
                    list.add(map(rs));
                return list;
            }
        }
    }

    public List<Transaction> listByMonth(Connection conn, String monthKey) throws SQLException {
        String sql = "SELECT id, amount, type, category_id, wallet_type, note, transaction_date, transaction_time, month_key, created_at, updated_at FROM transactions WHERE month_key = ? ORDER BY transaction_date DESC, transaction_time DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monthKey);
            try (ResultSet rs = ps.executeQuery()) {
                List<Transaction> list = new ArrayList<>();
                while (rs.next())
                    list.add(map(rs));
                return list;
            }
        }
    }

    private static Transaction map(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getString("id"));
        t.setAmount(rs.getLong("amount"));
        t.setType(rs.getString("type"));
        t.setCategoryId(rs.getString("category_id"));
        t.setWalletType(rs.getString("wallet_type"));
        t.setNote(rs.getString("note"));
        String d = rs.getString("transaction_date");
        if (d != null)
            t.setTransactionDate(LocalDate.parse(d));
        String ti = rs.getString("transaction_time");
        if (ti != null)
            t.setTransactionTime(LocalTime.parse(ti.length() > 8 ? ti.substring(0, 8) : ti));
        t.setMonthKey(rs.getString("month_key"));
        String c = rs.getString("created_at");
        if (c != null)
            t.setCreatedAt(DateUtil.parseInstant(c));
        String u = rs.getString("updated_at");
        if (u != null)
            t.setUpdatedAt(DateUtil.parseInstant(u));
        return t;
    }
}
