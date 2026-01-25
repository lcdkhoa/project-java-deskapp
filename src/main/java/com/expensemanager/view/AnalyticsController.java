package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;
import com.expensemanager.util.MonthKeyUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * Analytics - Section 4. Spending Trend, Forecast, Habits, Category Analysis, Tips.
 */
public class AnalyticsController {
    private final AnalyticsView view;
    private final JPanel contentPanel;

    public AnalyticsController(AnalyticsView view) {
        this.view = view;
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Analytics: insights, habits, category analysis, tips."), BorderLayout.CENTER);
    }

    public JPanel getContentPanel() { return contentPanel; }

    void refresh() {
        contentPanel.removeAll();
        String userId = AppContext.getUserId();
        YearMonth now = YearMonth.now();
        String curKey = MonthKeyUtil.of(now);
        String prevKey = MonthKeyUtil.of(now.minusMonths(1));
        try (Connection conn = DatabaseConnection.getConnection()) {
            TransactionDAO txDao = new TransactionDAO();
            long curExp = Math.abs(txDao.getMonthlyExpense(conn, userId, curKey));
            long prevExp = Math.abs(txDao.getMonthlyExpense(conn, userId, prevKey));
            double trend = prevExp != 0 ? (curExp - prevExp) * 100.0 / prevExp : Double.NaN;

            JPanel p = new JPanel(new GridLayout(2, 1));
            p.add(new JLabel("Spending Trend: " + (Double.isNaN(trend) ? "-" : String.format("%.1f%%", trend)) + " vs last month"));
            int dayOfMonth = java.time.LocalDate.now().getDayOfMonth();
            int daysPassed = Math.min(dayOfMonth, now.lengthOfMonth());
            int daysLeft = Math.max(0, now.lengthOfMonth() - daysPassed);
            long avgDaily = daysPassed > 0 ? curExp / daysPassed : 0;
            p.add(new JLabel("Monthly Forecast: " + com.expensemanager.util.CurrencyUtil.format(avgDaily * daysLeft) + " (avg daily * " + daysLeft + " days left)"));
            contentPanel.add(p, BorderLayout.NORTH);
        } catch (SQLException ex) {
            contentPanel.add(new JLabel("Error: " + ex.getMessage()));
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
