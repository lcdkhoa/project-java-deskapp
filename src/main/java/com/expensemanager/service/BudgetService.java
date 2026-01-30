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

public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final TransactionDAO transactionDAO;

    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.categoryDAO = new CategoryDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public long getTotalBudget(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return budgetDAO.getTotalBudget(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long getTotalSpent(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return Math.abs(transactionDAO.getMonthlyExpense(conn, userId, monthKey));
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<Budget> getBudgetsByMonth(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return budgetDAO.findByUserAndMonth(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<BudgetDAO.BudgetUsedRow> getBudgetUsedPerCategory(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return budgetDAO.getBudgetUsedPerCategory(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Category> getAvailableCategoriesForBudget(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Budget> existingBudgets = budgetDAO.findByUserAndMonth(conn, userId, monthKey);
            Set<String> budgetedCategoryIds = new HashSet<>();
            for (Budget b : existingBudgets) {
                budgetedCategoryIds.add(b.getCategoryId());
            }

            List<Category> allExpenseCategories = categoryDAO.findByType(conn, "expense");

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

    public void createBudget(Budget budget) throws ServiceException {
        validateBudget(budget);

        try (Connection conn = DatabaseConnection.getConnection()) {
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

    public void updateBudget(Budget budget) throws ServiceException {
        validateBudgetAmount(budget.getAmount());

        try (Connection conn = DatabaseConnection.getConnection()) {
            budgetDAO.update(conn, budget);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update budget: " + e.getMessage());
        }
    }

    public void deleteBudget(String budgetId) throws ServiceException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            budgetDAO.delete(conn, budgetId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete budget: " + e.getMessage());
        }
    }

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

    private void validateBudgetAmount(long amount) throws ServiceException {
        if (amount <= 0) {
            throw new ServiceException("Budget amount must be greater than 0.");
        }
        if (amount > 500_000_000L) {
            throw new ServiceException("Budget amount cannot exceed 500,000,000.");
        }
    }

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

    public BudgetSummary getBudgetSummary(String userId, String monthKey) {
        long totalBudget = getTotalBudget(userId, monthKey);
        long totalSpent = getTotalSpent(userId, monthKey);
        return new BudgetSummary(totalBudget, totalSpent);
    }

    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
    }
}
