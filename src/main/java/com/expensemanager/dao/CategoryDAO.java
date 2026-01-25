package com.expensemanager.dao;

import com.expensemanager.model.Category;
import com.expensemanager.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for categories table. Section 6.3.2.
 */
public class CategoryDAO {

    public List<Category> findAll(Connection conn) throws SQLException {
        String sql = "SELECT id, name, icon, color, type, is_active, created_at FROM categories WHERE is_active = 1 ORDER BY type, name";
        return queryList(conn, sql);
    }

    public List<Category> findByType(Connection conn, String type) throws SQLException {
        String sql = "SELECT id, name, icon, color, type, is_active, created_at FROM categories WHERE is_active = 1 AND type = ? ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            return extractList(ps);
        }
    }

    public Category findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, name, icon, color, type, is_active, created_at FROM categories WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    private List<Category> queryList(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return extractList(ps);
        }
    }

    private List<Category> extractList(PreparedStatement ps) throws SQLException {
        List<Category> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private static Category map(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getString("id"));
        c.setName(rs.getString("name"));
        c.setIcon(rs.getString("icon"));
        c.setColor(rs.getString("color"));
        c.setType(rs.getString("type"));
        c.setActive(rs.getInt("is_active") != 0);
        String t = rs.getString("created_at");
        if (t != null) c.setCreatedAt(DateUtil.parseInstant(t));
        return c;
    }
}
