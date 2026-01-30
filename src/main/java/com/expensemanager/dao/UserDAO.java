package com.expensemanager.dao;

import com.expensemanager.model.User;
import com.expensemanager.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, email, created_at FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    private static User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getString("id"));
        u.setEmail(rs.getString("email"));
        String t = rs.getString("created_at");
        if (t != null)
            u.setCreatedAt(DateUtil.parseInstant(t));
        return u;
    }
}
