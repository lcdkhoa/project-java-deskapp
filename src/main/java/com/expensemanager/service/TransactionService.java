package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.MonthKeyUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service layer for Transaction operations.
 * Encapsulates business logic and data access for transactions.
 */
public class TransactionService {

    private final TransactionDAO transactionDAO;

    // Validation constants
    private static final long MAX_AMOUNT = 500_000_000L;
    private static final int MAX_NOTE_LENGTH = 120;
    private static final int MAX_DATE_RANGE_DAYS = 60;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Get monthly expense total.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return expense amount (negative value from DB, but returned as absolute)
     */
    public long getMonthlyExpense(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return Math.abs(transactionDAO.getMonthlyExpense(conn, userId, monthKey));
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get monthly income total.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return income amount
     */
    public long getMonthlyIncome(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.getMonthlyIncome(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get cashflow by day for a month.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return map of date -> cashflow amount
     */
    public Map<LocalDate, Long> getCashflowByDay(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.getCashflowByDay(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    /**
     * Get expense by category for a month.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return map of categoryId -> expense amount
     */
    public Map<String, Long> getExpenseByCategory(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.getExpenseByCategory(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    /**
     * Get expense by date range (for bar chart).
     * 
     * @param userId user ID
     * @param start  start date
     * @param end    end date
     * @return map of date -> expense amount
     */
    public Map<LocalDate, Long> getExpenseByDateRange(String userId, LocalDate start, LocalDate end) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.getExpenseByDateRange(conn, userId, start, end);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    /**
     * Search and filter transactions.
     * 
     * @param userId     user ID
     * @param categoryId category filter (null for all)
     * @param walletType wallet type filter (null for all)
     * @param startDate  start date filter (null for no limit)
     * @param endDate    end date filter (null for no limit)
     * @param sortKey    sort key (date_desc, date_asc, amount_desc, amount_asc)
     * @param searchNote note search text (null for no filter)
     * @return list of transactions
     * @throws ServiceException if validation fails
     */
    public List<Transaction> searchTransactions(String userId, String categoryId, String walletType,
            LocalDate startDate, LocalDate endDate, String sortKey, String searchNote) throws ServiceException {

        // Validate date range
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                throw new ServiceException("End date must be on or after start date.");
            }
            if (startDate.plusDays(MAX_DATE_RANGE_DAYS).isBefore(endDate)) {
                throw new ServiceException("Date range cannot exceed " + MAX_DATE_RANGE_DAYS + " days.");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.search(conn, userId, categoryId, walletType, startDate, endDate, sortKey, searchNote);
        } catch (SQLException e) {
            throw new ServiceException("Failed to search transactions: " + e.getMessage());
        }
    }

    /**
     * Get transactions for a specific month.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return list of transactions
     */
    public List<Transaction> getTransactionsByMonth(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.listByMonth(conn, userId, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Create a new transaction.
     * 
     * @param transaction transaction to create
     * @throws ServiceException if validation fails or database error
     */
    public void createTransaction(Transaction transaction) throws ServiceException {
        validateTransaction(transaction);

        // Ensure amount sign matches type
        normalizeTransactionAmount(transaction);

        // Set month key from transaction date
        if (transaction.getTransactionDate() != null && transaction.getMonthKey() == null) {
            transaction.setMonthKey(MonthKeyUtil.fromDate(transaction.getTransactionDate()));
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            transactionDAO.insert(conn, transaction);
        } catch (SQLException e) {
            throw new ServiceException("Failed to create transaction: " + e.getMessage());
        }
    }

    /**
     * Update an existing transaction.
     * 
     * @param transaction transaction to update
     * @throws ServiceException if validation fails or database error
     */
    public void updateTransaction(Transaction transaction) throws ServiceException {
        validateTransaction(transaction);
        normalizeTransactionAmount(transaction);

        // Update month key if date changed
        if (transaction.getTransactionDate() != null) {
            transaction.setMonthKey(MonthKeyUtil.fromDate(transaction.getTransactionDate()));
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            transactionDAO.update(conn, transaction);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update transaction: " + e.getMessage());
        }
    }

    /**
     * Delete a transaction.
     * 
     * @param transactionId transaction ID to delete
     * @throws ServiceException if database error
     */
    public void deleteTransaction(String transactionId) throws ServiceException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            transactionDAO.delete(conn, transactionId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete transaction: " + e.getMessage());
        }
    }

    /**
     * Find transaction by ID.
     * 
     * @param transactionId transaction ID
     * @return transaction or null if not found
     */
    public Transaction findById(String transactionId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.findById(conn, transactionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Validate transaction before save.
     */
    private void validateTransaction(Transaction t) throws ServiceException {
        if (t.getUserId() == null || t.getUserId().isEmpty()) {
            throw new ServiceException("User ID is required.");
        }
        if (t.getType() == null || t.getType().isEmpty()) {
            throw new ServiceException("Transaction type is required.");
        }
        if (!t.getType().equals("expense") && !t.getType().equals("income")) {
            throw new ServiceException("Transaction type must be 'expense' or 'income'.");
        }
        if (t.getCategoryId() == null || t.getCategoryId().isEmpty()) {
            throw new ServiceException("Category is required.");
        }
        if (t.getWalletType() == null || t.getWalletType().isEmpty()) {
            throw new ServiceException("Wallet type is required.");
        }
        if (t.getTransactionDate() == null) {
            throw new ServiceException("Transaction date is required.");
        }
        if (t.getTransactionTime() == null) {
            t.setTransactionTime(LocalTime.now());
        }

        long absAmount = Math.abs(t.getAmount());
        if (absAmount <= 0) {
            throw new ServiceException("Amount must be greater than 0.");
        }
        if (absAmount > MAX_AMOUNT) {
            throw new ServiceException("Amount cannot exceed " + MAX_AMOUNT + ".");
        }

        // Truncate note if too long
        if (t.getNote() != null && t.getNote().length() > MAX_NOTE_LENGTH) {
            t.setNote(t.getNote().substring(0, MAX_NOTE_LENGTH));
        }
    }

    /**
     * Normalize transaction amount: expense should be negative, income positive.
     */
    private void normalizeTransactionAmount(Transaction t) {
        long absAmount = Math.abs(t.getAmount());
        if ("expense".equals(t.getType())) {
            t.setAmount(-absAmount);
        } else {
            t.setAmount(absAmount);
        }
    }

    /**
     * Convert wallet type code to display string.
     * 
     * @param walletType wallet type code
     * @return display string
     */
    public static String toWalletDisplay(String walletType) {
        if (walletType == null || walletType.isBlank()) {
            return "";
        }
        switch (walletType) {
            case "cash":
                return "Cash";
            case "bank_transfer":
                return "Bank Transfer";
            case "card":
                return "Card";
            case "e_wallet":
                return "E-wallet";
            default:
                return walletType;
        }
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
