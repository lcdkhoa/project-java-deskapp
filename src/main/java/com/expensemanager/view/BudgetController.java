package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.MonthKeyUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Budget - Section 3. Monthly overview, Budget by category.
 */
public class BudgetController {
    private final BudgetView view;
    private final JPanel contentPanel;
    private YearMonth currentMonth = YearMonth.now();

    public BudgetController(BudgetView view) {
        this.view = view;
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Budget: Monthly overview and by category. (Add Budget to start)"), BorderLayout.CENTER);
    }

    public JPanel getContentPanel() { return contentPanel; }

    void openAddBudget() {
        new AddBudgetDialog(view.getMain(), currentMonth).setVisible(true);
    }

    void refresh() {
        contentPanel.removeAll();
        String userId = AppContext.getUserId();
        String monthKey = MonthKeyUtil.of(currentMonth);
        try (Connection conn = DatabaseConnection.getConnection()) {
            TransactionDAO txDao = new TransactionDAO();
            BudgetDAO bDao = new BudgetDAO();
            CategoryDAO cDao = new CategoryDAO();

            long totalBudget = bDao.getTotalBudget(conn, userId, monthKey);
            long totalSpent = Math.abs(txDao.getMonthlyExpense(conn, userId, monthKey));
            long remaining = totalBudget - totalSpent;
            double pct = totalBudget > 0 ? totalSpent * 100.0 / totalBudget : 0;

            JPanel overview = new JPanel(new GridLayout(1, 3, 12, 0));
            overview.add(box("Total Budget", CurrencyUtil.format(totalBudget)));
            overview.add(box("Total Spent", CurrencyUtil.format(totalSpent)));
            overview.add(box("Remaining", CurrencyUtil.format(remaining)));
            contentPanel.add(overview, BorderLayout.NORTH);

            JPanel byCat = new JPanel();
            byCat.setLayout(new BoxLayout(byCat, BoxLayout.Y_AXIS));
            byCat.add(new JLabel("Budget by Category"));
            Map<String, String> idToName = new HashMap<>();
            for (Category c : cDao.findAll(conn)) idToName.put(c.getId(), c.getIcon() + " " + c.getName());
            for (BudgetDAO.BudgetUsedRow r : bDao.getBudgetUsedPerCategory(conn, userId, monthKey)) {
                byCat.add(new JLabel(idToName.getOrDefault(r.categoryId, r.categoryId) + "  " + CurrencyUtil.format(r.spent) + " / " + CurrencyUtil.format(r.budget) + "  " + String.format("%.0f%%", r.percentUsed)));
            }
            contentPanel.add(new JScrollPane(byCat), BorderLayout.CENTER);
        } catch (SQLException ex) {
            contentPanel.add(new JLabel("Error: " + ex.getMessage()));
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel box(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JLabel(value), BorderLayout.CENTER);
        return p;
    }
}
