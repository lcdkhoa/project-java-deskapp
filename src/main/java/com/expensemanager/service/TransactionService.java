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

public class TransactionService {

    private final TransactionDAO transactionDAO;

    private static final long MAX_AMOUNT = 500_000_000L;
    private static final int MAX_NOTE_LENGTH = 120;
    private static final int MAX_DATE_RANGE_DAYS = 60;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
    }

    public long getMonthlyExpense(String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return Math.abs(transactionDAO.getMonthlyExpense(conn, monthKey));
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long getMonthlyIncome(String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.getMonthlyIncome(conn, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public Map<LocalDate, Long> getCashflowByDay(String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.getCashflowByDay(conn, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public Map<String, Long> getExpenseByCategory(String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.getExpenseByCategory(conn, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public Map<LocalDate, Long> getExpenseByDateRange(LocalDate start, LocalDate end) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.getExpenseByDateRange(conn, start, end);
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public List<Transaction> searchTransactions(String categoryId, String walletType,
            LocalDate startDate, LocalDate endDate, String sortKey, String searchNote) throws ServiceException {

        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                throw new ServiceException("End date must be on or after start date.");
            }
            if (startDate.plusDays(MAX_DATE_RANGE_DAYS).isBefore(endDate)) {
                throw new ServiceException("Date range cannot exceed " + MAX_DATE_RANGE_DAYS + " days.");
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.search(conn, categoryId, walletType, startDate, endDate, sortKey, searchNote);
        } catch (SQLException e) {
            throw new ServiceException("Failed to search transactions: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactionsByMonth(String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.listByMonth(conn, monthKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void createTransaction(Transaction transaction) throws ServiceException {
        validateTransaction(transaction);

        normalizeTransactionAmount(transaction);

        if (transaction.getTransactionDate() != null && transaction.getMonthKey() == null) {
            transaction.setMonthKey(MonthKeyUtil.fromDate(transaction.getTransactionDate()));
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            transactionDAO.insert(conn, transaction);
        } catch (SQLException e) {
            throw new ServiceException("Failed to create transaction: " + e.getMessage());
        }
    }

    public void updateTransaction(Transaction transaction) throws ServiceException {
        validateTransaction(transaction);
        normalizeTransactionAmount(transaction);

        if (transaction.getTransactionDate() != null) {
            transaction.setMonthKey(MonthKeyUtil.fromDate(transaction.getTransactionDate()));
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            transactionDAO.update(conn, transaction);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update transaction: " + e.getMessage());
        }
    }

    public void deleteTransaction(String transactionId) throws ServiceException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            transactionDAO.delete(conn, transactionId);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete transaction: " + e.getMessage());
        }
    }

    public Transaction findById(String transactionId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return transactionDAO.findById(conn, transactionId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void validateTransaction(Transaction t) throws ServiceException {
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

        if (t.getNote() != null && t.getNote().length() > MAX_NOTE_LENGTH) {
            t.setNote(t.getNote().substring(0, MAX_NOTE_LENGTH));
        }
    }

    private void normalizeTransactionAmount(Transaction t) {
        long absAmount = Math.abs(t.getAmount());
        if ("expense".equals(t.getType())) {
            t.setAmount(-absAmount);
        } else {
            t.setAmount(absAmount);
        }
    }

    private static Map<String, String> walletDisplayCache;

    public static String toWalletDisplay(String walletType) {
        if (walletType == null || walletType.isBlank()) {
            return "";
        }

        if (walletDisplayCache == null) {
            WalletTypeService walletTypeService = new WalletTypeService();
            walletDisplayCache = walletTypeService.getWalletDisplayNameMap();
        }

        return walletDisplayCache.getOrDefault(walletType, walletType);
    }

    public static void clearWalletDisplayCache() {
        walletDisplayCache = null;
    }

    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
    }
}
