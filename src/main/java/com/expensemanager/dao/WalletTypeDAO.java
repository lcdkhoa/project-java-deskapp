package com.expensemanager.dao;

import com.expensemanager.model.WalletType;
import com.expensemanager.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WalletTypeDAO {

    public List<WalletType> findAll(Connection conn) throws SQLException {
        String sql = "SELECT id, name, display_name, icon_path, sort_order, is_active, created_at " +
                "FROM wallet_types WHERE is_active = 1 ORDER BY sort_order";
        return queryList(conn, sql);
    }

    public WalletType findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, name, display_name, icon_path, sort_order, is_active, created_at " +
                "FROM wallet_types WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    public WalletType findByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT id, name, display_name, icon_path, sort_order, is_active, created_at " +
                "FROM wallet_types WHERE name = ? AND is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return map(rs);
            }
        }
        return null;
    }

    private List<WalletType> queryList(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return extractList(ps);
        }
    }

    private List<WalletType> extractList(PreparedStatement ps) throws SQLException {
        List<WalletType> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(map(rs));
        }
        return list;
    }

    private static WalletType map(ResultSet rs) throws SQLException {
        WalletType w = new WalletType();
        w.setId(rs.getString("id"));
        w.setName(rs.getString("name"));
        w.setDisplayName(rs.getString("display_name"));
        w.setIconPath(rs.getString("icon_path"));
        w.setSortOrder(rs.getInt("sort_order"));
        w.setActive(rs.getInt("is_active") != 0);
        String t = rs.getString("created_at");
        if (t != null)
            w.setCreatedAt(DateUtil.parseInstant(t));
        return w;
    }
}
