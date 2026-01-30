package com.expensemanager.service;

import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<Category> getAllCategories() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoryDAO.findAll(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Category> getCategoriesByType(String type) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoryDAO.findByType(conn, type);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Category> getExpenseCategories() {
        return getCategoriesByType("expense");
    }

    public List<Category> getIncomeCategories() {
        return getCategoriesByType("income");
    }

    public Category findById(String id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoryDAO.findById(conn, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Category> getCategoryMap() {
        Map<String, Category> map = new HashMap<>();
        for (Category c : getAllCategories()) {
            map.put(c.getId(), c);
        }
        return map;
    }

    public Map<String, String> getCategoryNameMap() {
        Map<String, String> map = new HashMap<>();
        for (Category c : getAllCategories()) {
            map.put(c.getId(), c.getName());
        }
        return map;
    }
}
