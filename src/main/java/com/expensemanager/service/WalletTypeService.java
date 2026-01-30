package com.expensemanager.service;

import com.expensemanager.dao.WalletTypeDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.WalletType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletTypeService {

    private final WalletTypeDAO walletTypeDAO;

    public WalletTypeService() {
        this.walletTypeDAO = new WalletTypeDAO();
    }

    public List<WalletType> getAllWalletTypes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return walletTypeDAO.findAll(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public WalletType findById(String id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return walletTypeDAO.findById(conn, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public WalletType findByName(String name) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return walletTypeDAO.findByName(conn, name);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, WalletType> getWalletTypeMap() {
        Map<String, WalletType> map = new HashMap<>();
        for (WalletType w : getAllWalletTypes()) {
            map.put(w.getName(), w);
        }
        return map;
    }

    public Map<String, String> getWalletDisplayNameMap() {
        Map<String, String> map = new HashMap<>();
        for (WalletType w : getAllWalletTypes()) {
            map.put(w.getName(), w.getDisplayName());
        }
        return map;
    }
}
