package com.expensemanager.service;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer for Budget operations.
 * Encapsulates business logic and data access for budgets.
 */
public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;

    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.categoryDAO = new CategoryDAO();
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Get total budget for a user in a specific month.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return total budget amount
     */
    public long getTotalBudget(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return budgetDAO.getTotalBudget(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get total spent (expenses) for a user in a specific month.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return total spent amount (absolute value)
     */
    public long getTotalSpent(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return Math.abs(transactionDAO.getMonthlyExpense(conn, userId, monthKey));
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get budgets for a user in a specific month.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return list of budgets
     */
    public List<Budget> getBudgetsByMonth(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return budgetDAO.findByUserAndMonth(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get budget usage per category for a user in a specific month.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return list of BudgetUsedRow (categoryId, budget, spent, percentUsed)
     */
    public List<BudgetDAO.BudgetUsedRow> getBudgetUsedPerCategory(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return budgetDAO.getBudgetUsedPerCategory(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get categories that are available for adding a new budget
     * (categories that don't have a budget yet for the given month).
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return list of available expense categories
     */
    public List<Category> getAvailableCategoriesForBudget(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get existing budgets for this month
            List<Budget> existingBudgets = budgetDAO.findByUserAndMonth(conn, userId, monthKey);
            Set<String> budgetedCategoryIds = new HashSet<>();
            for (Budget b : existingBudgets) {
                budgetedCategoryIds.add(b.getCategoryId());
            }

            // Get all expense categories
            List<Category> allExpenseCategories = categoryDAO.findByType(conn, "expense");

            // Filter out categories that already have budgets
            List<Category> available = new ArrayList<>();
            for (Category c : allExpenseCategories) {
                if (!budgetedCategoryIds.contains(c.getId())) {
                    available.add(c);
                }
            }
            return available;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Create a new budget.
     * 
     * @param budget budget to create
     * @throws ServiceException if validation fails or database error
     */
    public void createBudget(Budget budget) throws ServiceException {
        validateBudget(budget);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if budget already exists for this user/category/month
            Budget existing = budgetDAO.findByUserCategoryMonth(conn, budget.getUserId(),
                    budget.getCategoryId(), budget.getMonthKey());
            if (existing != null) {
                throw new ServiceException("Budget already exists for this category and month.");
            }
            budgetDAO.insert(conn, budget);
        } catch (SQLException e) {
            throw new ServiceException("Failed to create budget: " + e.getMessage());
        }
    }

    /**
     * Update an existing budget.
     * 
     * @param budget budget to update
     * @throws ServiceException if validation fails or database error
     */
    public void updateBudget(Budget budget) throws ServiceException {
        validateBudgetAmount(budget.getAmount());

        try (Connection conn = DatabaseConnection.getConnection()) {
            budgetDAO.update(conn, budget);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update budget: " + e.getMessage());
        }
    }

    /**
     * Delete a budget.
     * 
     * @param budgetId budget ID to delete
     * @throws ServiceException if database error
     */
    public void deleteBudget(String budgetId) throws ServiceException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            budgetDAO.delete(conn, budgetId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete budget: " + e.getMessage());
        }
    }

    /**
     * Validate budget before save.
     */
    private void validateBudget(Budget budget) throws ServiceException {
        if (budget.getUserId() == null || budget.getUserId().isEmpty()) {
            throw new ServiceException("User ID is required.");
        }
        if (budget.getCategoryId() == null || budget.getCategoryId().isEmpty()) {
            throw new ServiceException("Category is required.");
        }
        if (budget.getMonthKey() == null || budget.getMonthKey().isEmpty()) {
            throw new ServiceException("Month is required.");
        }
        validateBudgetAmount(budget.getAmount());
    }

    /**
     * Validate budget amount.
     */
    private void validateBudgetAmount(long amount) throws ServiceException {
        if (amount <= 0) {
            throw new ServiceException("Budget amount must be greater than 0.");
        }
        if (amount > 500_000_000L) {
            throw new ServiceException("Budget amount cannot exceed 500,000,000.");
        }
    }

    /**
     * Data class for budget summary.
     */
    public static class BudgetSummary {
        public final long totalBudget;
        public final long totalSpent;
        public final long remaining;
        public final double spentPercent;

        public BudgetSummary(long totalBudget, long totalSpent) {
            this.totalBudget = totalBudget;
            this.totalSpent = totalSpent;
            this.remaining = totalBudget - totalSpent;
            this.spentPercent = totalBudget > 0 ? (totalSpent * 100.0 / totalBudget) : 0;
        }
    }

    /**
     * Get budget summary for a user in a specific month.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return budget summary
     */
    public BudgetSummary getBudgetSummary(String userId, String monthKey) {
        long totalBudget = getTotalBudget(userId, monthKey);
        long totalSpent = getTotalSpent(userId, monthKey);
        return new BudgetSummary(totalBudget, totalSpent);
    }

    /**
     * Custom exception for service layer errors.
     */
    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
    }
}
