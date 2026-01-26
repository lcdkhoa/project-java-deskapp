package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.DateUtil;
import com.expensemanager.util.MonthKeyUtil;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Transactions list. Section 2.2: grouped by date (Today, Yesterday, Dec 19, 2025),
 * date desc, time desc. Custom card-style rows.
 */
public class TransactionsController {
    private final TransactionsView view;
    private final JPanel listPanel;
    private final JScrollPane listScroll;
    private static final int ROW_GAP = 8;
    private static final int HEADER_GAP = 4;

    private static final Map<String, String> WALLET_DISPLAY = Map.of(
            "cash", "Cash", "bank_transfer", "Bank Transfer", "card", "Card", "e_wallet", "E-wallet");

    public TransactionsController(TransactionsView view) {
        this.view = view;
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(0xF3F4F6));
        listPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        listScroll = new JScrollPane(listPanel);
        listScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        listScroll.getViewport().setBackground(new Color(0xF3F4F6));
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
        return listScroll;
    }

    void refresh() {
        String userId = AppContext.getUserId();
        String monthKey = MonthKeyUtil.of(java.time.YearMonth.now());
        listPanel.removeAll();
        try (Connection conn = DatabaseConnection.getConnection()) {
            TransactionDAO txDao = new TransactionDAO();
            CategoryDAO cDao = new CategoryDAO();
            Map<String, Category> idToCat = new HashMap<>();
            for (Category c : cDao.findAll(conn)) idToCat.put(c.getId(), c);

            List<Transaction> list = txDao.listByMonth(conn, userId, monthKey);
            if (list.isEmpty()) {
                JLabel empty = new JLabel("No transactions");
                empty.setForeground(new Color(0x6B7280));
                empty.setBorder(BorderFactory.createEmptyBorder(24, 12, 24, 12));
                listPanel.add(empty);
            }
            LocalDate lastDate = null;
            for (Transaction t : list) {
                LocalDate d = t.getTransactionDate();
                if (d != null && !d.equals(lastDate)) {
                    if (lastDate != null) listPanel.add(Box.createVerticalStrut(HEADER_GAP));
                    JLabel header = new JLabel(DateUtil.formatDateForGroup(d));
                    header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
                    header.setForeground(new Color(0x6B7280));
                    header.setAlignmentX(Component.LEFT_ALIGNMENT);
                    header.setBorder(BorderFactory.createEmptyBorder(4, 12, 2, 12));
                    listPanel.add(header);
                    listPanel.add(Box.createVerticalStrut(HEADER_GAP));
                    lastDate = d;
                }
                Category cat = idToCat.get(t.getCategoryId());
                String icon = cat != null ? cat.getIcon() : "•";
                Color iconColor = parseColor(cat != null ? cat.getColor() : null);
                String wallet = WALLET_DISPLAY.getOrDefault(t.getWalletType(), t.getWalletType() != null ? t.getWalletType() : "");
                String time = t.getTransactionTime() != null ? t.getTransactionTime().toString().substring(0, 5) : "";
                TransactionRowPanel row = new TransactionRowPanel(
                        icon, iconColor, t.getNote(), wallet, t.getAmount(), time);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                listPanel.add(row);
                listPanel.add(Box.createVerticalStrut(ROW_GAP));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Error loading transactions: " + ex.getMessage());
        }
        listPanel.revalidate();
        listPanel.repaint();
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
