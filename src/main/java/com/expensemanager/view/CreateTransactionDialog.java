package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.MonthKeyUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Create Transaction modal - Section 2.1. Amount, Type, Date, Time, Category, Wallet (optional), Note (max 120).
 * Expense → amount &lt; 0; Income → amount &gt; 0.
 */
public class CreateTransactionDialog extends JDialog {
    private final MainFrame main;
    private JTextField amountF;
    private JComboBox<String> typeCombo;
    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private JComboBox<CategoryItem> categoryCombo;
    private JComboBox<String> walletCombo;
    private JTextField noteF;

    private static final String[] WALLET_OPTIONS = {"Cash", "Bank Transfer", "Card", "E-wallet"};
    private static final String[] WALLET_VALUES = {"cash", "bank_transfer", "card", "e_wallet"};

    public CreateTransactionDialog(MainFrame main) {
        super(main, "Create Transaction", true);
        this.main = main;
        setSize(420, 420);
        setLocationRelativeTo(main);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int row = 0;

        // Amount
        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Amount *"), g);
        g.gridx = 1; amountF = new JTextField(15); form.add(amountF, g);

        // Type
        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Type *"), g);
        typeCombo = new JComboBox<>(new String[]{"Expense", "Income"});
        typeCombo.addActionListener(e -> refillCategories());
        g.gridx = 1; form.add(typeCombo, g);

        // Date
        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Date *"), g);
        dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        g.gridx = 1; form.add(dateSpinner, g);

        // Time
        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Time *"), g);
        timeSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE));
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        g.gridx = 1; form.add(timeSpinner, g);

        // Category
        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Category *"), g);
        categoryCombo = new JComboBox<>();
        g.gridx = 1; form.add(categoryCombo, g);

        // Wallet (optional)
        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Wallet"), g);
        walletCombo = new JComboBox<>(WALLET_OPTIONS);
        g.gridx = 1; form.add(walletCombo, g);

        // Note (max 120) - truncated on save
        g.gridy = row++; g.gridx = 0; form.add(new JLabel("Note (max 120)"), g);
        noteF = new JTextField(20);
        g.gridx = 1; form.add(noteF, g);

        refillCategories();

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
        String type = typeCombo.getSelectedItem().toString().toLowerCase();
        categoryCombo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Category> list = new CategoryDAO().findByType(conn, type);
            for (Category c : list) {
                categoryCombo.addItem(new CategoryItem(c.getId(), c.getName(), c.getIcon()));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + ex.getMessage());
        }
    }

    private void onSave() {
        String amountS = amountF.getText().trim();
        if (amountS.isEmpty()) { JOptionPane.showMessageDialog(this, "Amount is required."); return; }
        long amount;
        try {
            amount = Long.parseLong(amountS.replaceAll("[.,\\s]", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount."); return;
        }
        if (amount <= 0) { JOptionPane.showMessageDialog(this, "Amount must be positive."); return; }

        String type = typeCombo.getSelectedItem().toString().toLowerCase();
        if (type.equals("expense")) amount = -amount;

        CategoryItem cat = (CategoryItem) categoryCombo.getSelectedItem();
        if (cat == null) { JOptionPane.showMessageDialog(this, "Select a category."); return; }

        LocalDate d = ((Date) dateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime t = ((Date) timeSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        String wallet = WALLET_VALUES[walletCombo.getSelectedIndex()];
        String note = noteF.getText();
        if (note != null && note.length() > 120) note = note.substring(0, 120);

        Transaction tx = new Transaction();
        tx.setUserId(AppContext.getUserId());
        tx.setAmount(amount);
        tx.setType(type);
        tx.setCategoryId(cat.id);
        tx.setWalletType(wallet);
        tx.setNote(note.isEmpty() ? null : note);
        tx.setTransactionDate(d);
        tx.setTransactionTime(t);
        tx.setMonthKey(MonthKeyUtil.of(d));

        try (Connection conn = DatabaseConnection.getConnection()) {
            new TransactionDAO().insert(conn, tx);
            main.refreshDashboard();
            main.refreshTransactions();
            main.refreshBudget();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }

    private static class CategoryItem {
        final String id;
        final String name;
        final String icon;
        CategoryItem(String id, String name, String icon) { this.id = id; this.name = name; this.icon = icon; }
        @Override
        public String toString() { return icon + " " + name; }
    }
}
