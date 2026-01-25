package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.util.MonthKeyUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Add Budget - Section 3.1. Category (expense, not already budgeted), Monthly Budget Amount (max 500.000.000).
 */
public class AddBudgetDialog extends JDialog {
    private final MainFrame main;
    private final YearMonth month;
    private JComboBox<Category> categoryCombo;
    private JTextField amountF;

    public AddBudgetDialog(MainFrame main, YearMonth month) {
        super(main, "Add Budget", true);
        this.main = main;
        this.month = month;
        setSize(380, 180);
        setLocationRelativeTo(main);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int row = 0;
        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Category *"), g);
        categoryCombo = new JComboBox<>();
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Category) setText(((Category)value).getIcon() + " " + ((Category)value).getName());
                return this;
            }
        });
        refillCategories();
        g.gridx = 1; form.add(categoryCombo, g);

        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Monthly Budget Amount * (max 500.000.000 đ)"), g);
        amountF = new JTextField(15);
        g.gridx = 1; form.add(amountF, g);

        JPanel buttons = new JPanel(new FlowLayout());
        JButton save = new JButton("Save");
        save.addActionListener(e -> onSave());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        buttons.add(save);
        buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void refillCategories() {
        categoryCombo.removeAllItems();
        String monthKey = MonthKeyUtil.of(month);
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Budget> existing = new BudgetDAO().findByUserAndMonth(conn, AppContext.getUserId(), monthKey);
            Set<String> having = new HashSet<>();
            for (Budget b : existing) having.add(b.getCategoryId());
            List<Category> list = new CategoryDAO().findByType(conn, "expense");
            for (Category c : list) {
                if (!having.contains(c.getId())) categoryCombo.addItem(c);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void onSave() {
        Category c = (Category) categoryCombo.getSelectedItem();
        if (c == null) { JOptionPane.showMessageDialog(this, "Select a category."); return; }
        String a = amountF.getText().trim().replaceAll("[.,\\s]", "");
        if (a.isEmpty()) { JOptionPane.showMessageDialog(this, "Amount is required."); return; }
        long amount;
        try { amount = Long.parseLong(a); } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount."); return;
        }
        if (amount <= 0 || amount > 500_000_000L) {
            JOptionPane.showMessageDialog(this, "Amount must be 1..500.000.000."); return;
        }

        Budget b = new Budget();
        b.setUserId(AppContext.getUserId());
        b.setCategoryId(c.getId());
        b.setMonthKey(MonthKeyUtil.of(month));
        b.setAmount(amount);
        try (Connection conn = DatabaseConnection.getConnection()) {
            new BudgetDAO().insert(conn, b);
            main.refreshBudget();
            main.refreshDashboard();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }
}
