package com.expensemanager.view;

import com.expensemanager.FlatLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Main window. Menu: Dashboard, Transactions, Budget, Analytics. Theme toggle top-right. Section 2, 5.
 */
public class MainFrame extends JFrame {
    private static final int W = 1200;
    private static final int H = 750;

    private final JPanel cards;
    private final CardLayout cardLayout;
    private final DashboardView dashboardView;
    private final TransactionsView transactionsView;
    private final BudgetView budgetView;
    private final AnalyticsView analyticsView;

    public MainFrame() {
        setTitle("Personal Expense Manager");
        setSize(W, H);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        dashboardView = new DashboardView(this);
        transactionsView = new TransactionsView(this);
        budgetView = new BudgetView(this);
        analyticsView = new AnalyticsView(this);

        cards.add(dashboardView, "Dashboard");
        cards.add(transactionsView, "Transactions");
        cards.add(budgetView, "Budget");
        cards.add(analyticsView, "Analytics");

        JPanel north = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Personal Expense Manager");
        title.setFont(title.getFont().deriveFont(18f));
        north.add(title, BorderLayout.WEST);

        JButton themeBtn = new JButton(FlatLaf.isDark() ? "☀ Light" : "🌙 Dark");
        themeBtn.addActionListener(e -> {
            FlatLaf.toggleTheme();
            themeBtn.setText(FlatLaf.isDark() ? "☀ Light" : "🌙 Dark");
        });
        north.add(themeBtn, BorderLayout.EAST);
        add(north, BorderLayout.NORTH);

        JPanel west = new JPanel(new GridLayout(4, 1, 0, 4));
        west.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        west.add(btn("📊 Dashboard", "Dashboard"));
        west.add(btn("📝 Transactions", "Transactions"));
        west.add(btn("💰 Budget", "Budget"));
        west.add(btn("📈 Analytics", "Analytics"));

        JPanel westWrap = new JPanel(new BorderLayout());
        westWrap.add(west, BorderLayout.NORTH);
        add(westWrap, BorderLayout.WEST);
        add(cards, BorderLayout.CENTER);

        showCard("Dashboard");
    }

    private JButton btn(String label, String card) {
        JButton b = new JButton(label);
        b.addActionListener(e -> showCard(card));
        return b;
    }

    public void showCard(String name) {
        cardLayout.show(cards, name);
        dashboardView.onShown();
        transactionsView.onShown();
        budgetView.onShown();
        analyticsView.onShown();
    }

    public void openCreateTransaction() {
        new CreateTransactionDialog(this).setVisible(true);
    }

    public void refreshDashboard() { dashboardView.refresh(); }
    public void refreshTransactions() { transactionsView.refresh(); }
    public void refreshBudget() { budgetView.refresh(); }
}
