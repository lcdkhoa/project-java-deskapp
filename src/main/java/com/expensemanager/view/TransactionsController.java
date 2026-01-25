package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.MonthKeyUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Transactions list. Section 2.2 - grouped by date, date desc, time desc.
 */
public class TransactionsController {
    private final TransactionsView view;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private static final String[] COLS = {"Date", "Time", "Category", "Wallet", "Note", "Amount"};

    private static final Map<String, String> WALLET_DISPLAY = Map.of(
        "cash", "Cash", "bank_transfer", "Bank Transfer", "card", "Card", "e_wallet", "E-wallet");

    public TransactionsController(TransactionsView view) {
        this.view = view;
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public JPanel getFilterPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Search:"));
        p.add(new JTextField(20) {{
            setToolTipText("Search by note");
        }});
        p.add(new JLabel("Month:"));
        p.add(new JComboBox<>(new String[]{"Current"}));
        p.add(new JButton("Apply"));
        return p;
    }

    public JScrollPane getTableScroll() {
        return new JScrollPane(table);
    }

    void refresh() {
        String userId = AppContext.getUserId();
        String monthKey = MonthKeyUtil.of(YearMonth.now());
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            TransactionDAO txDao = new TransactionDAO();
            CategoryDAO cDao = new CategoryDAO();
            Map<String, String> idToName = new HashMap<>();
            for (Category c : cDao.findAll(conn)) idToName.put(c.getId(), c.getIcon() + " " + c.getName());

            List<Transaction> list = txDao.listByMonth(conn, userId, monthKey);
            for (Transaction t : list) {
                tableModel.addRow(new Object[]{
                    t.getTransactionDate() != null ? t.getTransactionDate().toString() : "",
                    t.getTransactionTime() != null ? t.getTransactionTime().toString().substring(0, 5) : "",
                    idToName.getOrDefault(t.getCategoryId(), t.getCategoryId()),
                    WALLET_DISPLAY.getOrDefault(t.getWalletType(), t.getWalletType()),
                    t.getNote() != null ? t.getNote() : "",
                    t.getAmount() < 0 ? "-" + CurrencyUtil.format(-t.getAmount()) : "+" + CurrencyUtil.format(t.getAmount())
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Error loading transactions: " + ex.getMessage());
        }
    }
}
