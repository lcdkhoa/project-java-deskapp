package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.MonthKeyUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashMap;
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

            JPanel overview = new JPanel(new GridLayout(1, 3, 12, 0));
            overview.add(box("Total Budget", CurrencyUtil.format(totalBudget)));
            overview.add(box("Total Spent", CurrencyUtil.format(totalSpent)));
            overview.add(box("Remaining", CurrencyUtil.format(remaining)));
            contentPanel.add(overview, BorderLayout.NORTH);

            JPanel byCat = new JPanel();
            byCat.setLayout(new BoxLayout(byCat, BoxLayout.Y_AXIS));
            byCat.setBackground(new Color(0xF3F4F6));
            byCat.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            JLabel sectionTitle = new JLabel("Budget by Category");
            sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 14f));
            sectionTitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            byCat.add(sectionTitle);

            Map<String, Category> idToCat = new HashMap<>();
            for (Category c : cDao.findAll(conn)) idToCat.put(c.getId(), c);

            for (BudgetDAO.BudgetUsedRow r : bDao.getBudgetUsedPerCategory(conn, userId, monthKey)) {
                Category cat = idToCat.get(r.categoryId);
                String icon = cat != null ? cat.getIcon() : "•";
                Color iconColor = parseColor(cat != null ? cat.getColor() : null);
                String name = cat != null ? cat.getName() : r.categoryId;
                BudgetCategoryCard card = new BudgetCategoryCard(icon, iconColor, name, r.spent, r.budget, r.percentUsed);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                byCat.add(card);
                byCat.add(Box.createVerticalStrut(10));
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

    private static Color parseColor(String hex) {
        if (hex == null || hex.isBlank()) return new Color(0x9CA3AF);
        if (!hex.startsWith("#")) hex = "#" + hex;
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return new Color(0x9CA3AF);
        }
    }
}
