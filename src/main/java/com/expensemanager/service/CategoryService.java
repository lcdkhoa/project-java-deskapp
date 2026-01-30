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

/**
 * Service layer for Category operations.
 * Encapsulates business logic and data access for categories.
 */
public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Get all active categories.
     * 
     * @return list of categories, empty if error
     */
    public List<Category> getAllCategories() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoryDAO.findAll(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Get categories by type (expense/income).
     * 
     * @param type "expense" or "income"
     * @return list of categories, empty if error
     */
    public List<Category> getCategoriesByType(String type) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoryDAO.findByType(conn, type);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Get expense categories.
     * 
     * @return list of expense categories
     */
    public List<Category> getExpenseCategories() {
        return getCategoriesByType("expense");
    }

    /**
     * Get income categories.
     * 
     * @return list of income categories
     */
    public List<Category> getIncomeCategories() {
        return getCategoriesByType("income");
    }

    /**
     * Find category by ID.
     * 
     * @param id category ID
     * @return category or null if not found
     */
    public Category findById(String id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return categoryDAO.findById(conn, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build a map from category ID to Category object.
     * 
     * @return map of id -> Category
     */
    public Map<String, Category> getCategoryMap() {
        Map<String, Category> map = new HashMap<>();
        for (Category c : getAllCategories()) {
            map.put(c.getId(), c);
        }
        return map;
    }

    /**
     * Build a map from category ID to category name.
     * 
     * @return map of id -> name
     */
    public Map<String, String> getCategoryNameMap() {
        Map<String, String> map = new HashMap<>();
        for (Category c : getAllCategories()) {
            map.put(c.getId(), c.getName());
        }
        return map;
    }
}
