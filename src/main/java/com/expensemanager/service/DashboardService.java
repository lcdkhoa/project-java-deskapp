package com.expensemanager.service;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for Dashboard operations.
 * Aggregates data from multiple DAOs for dashboard display.
 */
public class DashboardService {

    private final TransactionDAO transactionDAO;
    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;

    public DashboardService() {
        this.transactionDAO = new TransactionDAO();
        this.budgetDAO = new BudgetDAO();
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Get KPI data for dashboard.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return KPI data object
     */
    public KPIData getKPIData(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            long expense = transactionDAO.getMonthlyExpense(conn, userId, monthKey);
            long income = transactionDAO.getMonthlyIncome(conn, userId, monthKey);
            long remaining = income - Math.abs(expense);
            long totalBudget = budgetDAO.getTotalBudget(conn, userId, monthKey);
            double budgetUsedPct = totalBudget > 0 ? (Math.abs(expense) * 100.0 / totalBudget) : Double.NaN;

            return new KPIData(expense, income, remaining, totalBudget, budgetUsedPct);
        } catch (SQLException e) {
            e.printStackTrace();
            return new KPIData(0, 0, 0, 0, Double.NaN);
        }
    }

    /**
     * Get expense data for last 7 days (for bar chart).
     * 
     * @param userId        user ID
     * @param referenceDate reference date (today if current month, end of month
     *                      otherwise)
     * @return BarChartData with dates and amounts
     */
    public BarChartData getLast7DaysExpense(String userId, LocalDate referenceDate) {
        LocalDate start = referenceDate.minusDays(6);
        LocalDate end = referenceDate;

        try (Connection conn = DatabaseConnection.getConnection()) {
            Map<LocalDate, Long> byDate = transactionDAO.getExpenseByDateRange(conn, userId, start, end);

            List<LocalDate> dates = new ArrayList<>();
            List<Long> amounts = new ArrayList<>();
            boolean hasData = false;

            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                dates.add(d);
                long value = byDate.getOrDefault(d, 0L);
                amounts.add(value);
                if (value > 0) {
                    hasData = true;
                }
            }

            return new BarChartData(dates, amounts, hasData);
        } catch (SQLException e) {
            e.printStackTrace();
            return new BarChartData(List.of(), List.of(), false);
        }
    }

    /**
     * Get expense by category data (for donut chart).
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return CategoryChartData
     */
    public CategoryChartData getCategoryExpenseData(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Category> allCategories = categoryDAO.findByType(conn, "expense");
            Map<String, Long> expenseByCategory = transactionDAO.getExpenseByCategory(conn, userId, monthKey);

            Map<String, Category> idToCategory = new HashMap<>();
            Map<String, Long> categoryExpenses = new HashMap<>();
            long totalExpense = 0;

            for (Category cat : allCategories) {
                idToCategory.put(cat.getId(), cat);
                long expense = expenseByCategory.getOrDefault(cat.getId(), 0L);
                categoryExpenses.put(cat.getId(), expense);
                totalExpense += expense;
            }

            boolean hasData = totalExpense > 0;

            return new CategoryChartData(allCategories, idToCategory, categoryExpenses, totalExpense, hasData);
        } catch (SQLException e) {
            e.printStackTrace();
            return new CategoryChartData(List.of(), Map.of(), Map.of(), 0, false);
        }
    }

    /**
     * Get monthly cashflow data (for line chart) with separate Income and Expense
     * series.
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @param month    YearMonth object
     * @return MonthlyCashFlowData with income and expense per day
     */
    public MonthlyCashFlowData getMonthlyCashflow(String userId, String monthKey, YearMonth month) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Map<LocalDate, Long> incomeByDay = transactionDAO.getIncomeByDay(conn, userId, monthKey);
            Map<LocalDate, Long> expenseByDay = transactionDAO.getExpenseByDay(conn, userId, monthKey);

            int days = month.lengthOfMonth();
            List<Integer> dayNumbers = new ArrayList<>();
            List<Long> incomeValues = new ArrayList<>();
            List<Long> expenseValues = new ArrayList<>();
            boolean hasData = false;

            for (int i = 1; i <= days; i++) {
                LocalDate d = month.atDay(i);
                dayNumbers.add(i);
                long income = incomeByDay.getOrDefault(d, 0L);
                long expense = expenseByDay.getOrDefault(d, 0L);
                incomeValues.add(income);
                expenseValues.add(expense);
                if (income != 0 || expense != 0) {
                    hasData = true;
                }
            }

            return new MonthlyCashFlowData(dayNumbers, incomeValues, expenseValues, hasData, month);
        } catch (SQLException e) {
            e.printStackTrace();
            return new MonthlyCashFlowData(List.of(), List.of(), List.of(), false, month);
        }
    }

    /**
     * Get budget warnings (categories that are over budget).
     * 
     * @param userId   user ID
     * @param monthKey month key (yyyy-MM)
     * @return BudgetWarningsData
     */
    public BudgetWarningsData getBudgetWarnings(String userId, String monthKey) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<BudgetDAO.BudgetUsedRow> rows = budgetDAO.getBudgetUsedPerCategory(conn, userId, monthKey);
            List<BudgetDAO.BudgetUsedRow> overBudget = rows.stream()
                    .filter(r -> r.percentUsed >= 100)
                    .sorted((a, b) -> Double.compare(b.percentUsed, a.percentUsed))
                    .collect(Collectors.toList());

            Map<String, String> idToName = new HashMap<>();
            for (Category c : categoryDAO.findAll(conn)) {
                idToName.put(c.getId(), c.getName());
            }

            return new BudgetWarningsData(overBudget, idToName);
        } catch (SQLException e) {
            e.printStackTrace();
            return new BudgetWarningsData(List.of(), Map.of());
        }
    }

    // Data classes for dashboard

    /**
     * KPI data for dashboard cards.
     */
    public static class KPIData {
        public final long expense;
        public final long income;
        public final long remaining;
        public final long totalBudget;
        public final double budgetUsedPct;

        public KPIData(long expense, long income, long remaining, long totalBudget, double budgetUsedPct) {
            this.expense = expense;
            this.income = income;
            this.remaining = remaining;
            this.totalBudget = totalBudget;
            this.budgetUsedPct = budgetUsedPct;
        }
    }

    /**
     * Bar chart data for last 7 days.
     */
    public static class BarChartData {
        public final List<LocalDate> dates;
        public final List<Long> amounts;
        public final boolean hasData;

        public BarChartData(List<LocalDate> dates, List<Long> amounts, boolean hasData) {
            this.dates = dates;
            this.amounts = amounts;
            this.hasData = hasData;
        }
    }

    /**
     * Category expense data for donut chart.
     */
    public static class CategoryChartData {
        public final List<Category> categories;
        public final Map<String, Category> idToCategory;
        public final Map<String, Long> categoryExpenses;
        public final long totalExpense;
        public final boolean hasData;

        public CategoryChartData(List<Category> categories, Map<String, Category> idToCategory,
                Map<String, Long> categoryExpenses, long totalExpense, boolean hasData) {
            this.categories = categories;
            this.idToCategory = idToCategory;
            this.categoryExpenses = categoryExpenses;
            this.totalExpense = totalExpense;
            this.hasData = hasData;
        }
    }

    /**
     * Monthly Cash Flow chart data with separate Income and Expense series.
     */
    public static class MonthlyCashFlowData {
        public final List<Integer> dayNumbers;
        public final List<Long> incomeValues;
        public final List<Long> expenseValues;
        public final boolean hasData;
        public final YearMonth month;

        public MonthlyCashFlowData(List<Integer> dayNumbers, List<Long> incomeValues, List<Long> expenseValues,
                boolean hasData, YearMonth month) {
            this.dayNumbers = dayNumbers;
            this.incomeValues = incomeValues;
            this.expenseValues = expenseValues;
            this.hasData = hasData;
            this.month = month;
        }
    }

    /**
     * Budget warnings data.
     */
    public static class BudgetWarningsData {
        public final List<BudgetDAO.BudgetUsedRow> overBudgetItems;
        public final Map<String, String> categoryIdToName;

        public BudgetWarningsData(List<BudgetDAO.BudgetUsedRow> overBudgetItems, Map<String, String> categoryIdToName) {
            this.overBudgetItems = overBudgetItems;
            this.categoryIdToName = categoryIdToName;
        }

        public boolean hasWarnings() {
            return !overBudgetItems.isEmpty();
        }
    }
}
