package com.expensemanager.controller;

import com.expensemanager.util.AppContext;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.service.BudgetService;
import com.expensemanager.service.CategoryService;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.MonthKeyUtil;
import com.expensemanager.view.BudgetView.BudgetCard;
import com.expensemanager.view.BudgetView.BudgetDialog;
import com.expensemanager.view.BudgetView.BudgetDialogListener;
import com.expensemanager.view.BudgetView.DeleteBudgetDialog;
import com.expensemanager.view.BudgetView.BudgetProgressBar;
import com.expensemanager.view.BudgetView.BudgetRowItem;
import com.expensemanager.view.BudgetView.BudgetView;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetController implements BudgetDialogListener {
    private final BudgetView view;
    private final JPanel contentPanel;
    private YearMonth currentMonth = YearMonth.now();

    private final BudgetService budgetService;
    private final CategoryService categoryService;

    public BudgetController(BudgetView view) {
        this.view = view;
        this.budgetService = new BudgetService();
        this.categoryService = new CategoryService();

        contentPanel = new JPanel(new MigLayout("wrap 1, fillx, insets 20 20 20 20, gapy 14",
                "[grow]", "[pref!][pref!][grow]"));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setOpaque(true);
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void openAddBudget() {
        new BudgetDialog(view.getMain(), currentMonth, BudgetDialog.Mode.ADD, null, null, this).setVisible(true);
    }

    public void openEditBudget(Budget budget, Category category) {
        new BudgetDialog(view.getMain(), currentMonth, BudgetDialog.Mode.EDIT, budget, category, this).setVisible(true);
    }

    public void deleteBudget(Budget budget) {
        if (budget == null) {
            return;
        }
        final Budget toDelete = budget;
        Runnable onConfirm = () -> {
            try {
                budgetService.deleteBudget(toDelete.getId());
                onRefreshRequired();
            } catch (BudgetService.ServiceException ex) {
                JOptionPane.showMessageDialog(view.getMain(), ex.getMessage());
            }
        };
        new DeleteBudgetDialog(view.getMain(), onConfirm).setVisible(true);
    }

    @Override
    public List<Category> getAvailableCategories() {
        String userId = AppContext.getUserId();
        String monthKey = MonthKeyUtil.of(currentMonth);
        return budgetService.getAvailableCategoriesForBudget(userId, monthKey);
    }

    @Override
    public void onBudgetCreated(Budget budget) throws Exception {
        budgetService.createBudget(budget);
    }

    @Override
    public void onBudgetUpdated(Budget budget) throws Exception {
        budgetService.updateBudget(budget);
    }

    @Override
    public void onRefreshRequired() {
        view.getMain().refreshBudget();
        view.getMain().refreshDashboard();
    }

    public void refresh() {
        contentPanel.removeAll();
        String userId = AppContext.getUserId();
        String monthKey = MonthKeyUtil.of(currentMonth);

        try {
            BudgetService.BudgetSummary summary = budgetService.getBudgetSummary(userId, monthKey);
            long totalBudget = summary.totalBudget;
            long totalSpent = summary.totalSpent;
            long remaining = summary.remaining;

            JPanel summaryRow = new JPanel(
                    new MigLayout("ins 0, gap 20 0", "[grow,fill][grow,fill][grow,fill]", "[]"));
            summaryRow.setBackground(Color.WHITE);
            summaryRow.setOpaque(true);
            summaryRow.add(new BudgetCard("Total Budget", CurrencyUtil.format(totalBudget),
                    "src/main/java/com/expensemanager/img/budget/budget.png", new Color(0x2563EB)), "grow");
            summaryRow.add(new BudgetCard("Total Spent", CurrencyUtil.format(totalSpent),
                    "src/main/java/com/expensemanager/img/budget/spent.png", new Color(0xB91C1C)), "grow");
            summaryRow.add(new BudgetCard("Remaining", CurrencyUtil.format(remaining),
                    "src/main/java/com/expensemanager/img/budget/remains.png", new Color(0x16A34A)), "grow");
            contentPanel.add(summaryRow, "growx");

            double spentPercent = totalBudget > 0 ? (totalSpent * 100.0 / totalBudget) : 0;
            JPanel spentPanel = new JPanel(
                    new MigLayout("wrap 1, ins 20 24 20 24, fillx, gapy 8", "[grow,fill]", "[center][center]")) {
                private static final int ARC = 30;

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                    g2.setColor(new Color(0xE5E7EB));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            spentPanel.setOpaque(false);
            spentPanel.setPreferredSize(new Dimension(0, 85));
            spentPanel.setMinimumSize(new Dimension(0, 85));
            spentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

            JPanel textRow = new JPanel(new MigLayout("ins 0, fillx", "[pref!][grow][pref!]", "[center]"));
            textRow.setOpaque(false);
            JLabel spentLabel = new JLabel("Spent");
            spentLabel.setFont(spentLabel.getFont().deriveFont(Font.PLAIN, 14f));
            spentLabel.setForeground(new Color(0x111827));
            textRow.add(spentLabel);

            textRow.add(new JLabel(), "growx");

            JLabel percentLabel = new JLabel(String.format("%.0f%%", spentPercent));
            percentLabel.setFont(percentLabel.getFont().deriveFont(Font.BOLD, 14f));
            percentLabel.setForeground(spentPercent >= 100 ? new Color(0xB91C1C) : new Color(0x6B7280));
            textRow.add(percentLabel);
            spentPanel.add(textRow, "growx, aligny center");

            BudgetProgressBar spentBar = new BudgetProgressBar();
            spentBar.setPercent(spentPercent);
            spentPanel.add(spentBar, "growx, h 8!, aligny center");

            contentPanel.add(spentPanel, "growx");

            JPanel section = new JPanel(new MigLayout("wrap 1, fillx, insets 0, gapy 12", "[grow]", "[]"));
            section.setOpaque(false);

            JLabel sectionTitle = new JLabel("Budget by Category");
            sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.PLAIN, 16f));
            sectionTitle.setForeground(new Color(0x111827));
            section.add(sectionTitle, "growx");

            JPanel listPanel = new JPanel(new MigLayout("wrap 1, fillx, insets 0, gapy 15", "[grow]", "[]"));
            listPanel.setOpaque(false);

            Map<String, Category> idToCat = categoryService.getCategoryMap();

            Map<String, Budget> idToBudget = new HashMap<>();
            List<Budget> budgets = budgetService.getBudgetsByMonth(userId, monthKey);
            for (Budget b : budgets) {
                idToBudget.put(b.getCategoryId(), b);
            }

            for (BudgetDAO.BudgetUsedRow r : budgetService.getBudgetUsedPerCategory(userId, monthKey)) {
                Category cat = idToCat.get(r.categoryId);
                Budget budget = idToBudget.get(r.categoryId);
                BudgetRowItem row = new BudgetRowItem(cat, r, budget, this);
                listPanel.add(row, "growx");
            }

            JScrollPane scroll = new JScrollPane(listPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.setOpaque(false);
            section.add(scroll, "grow, push");

            contentPanel.add(section, "grow, push");
        } catch (Exception ex) {
            contentPanel.add(new JLabel("Error: " + ex.getMessage()), "growx");
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
