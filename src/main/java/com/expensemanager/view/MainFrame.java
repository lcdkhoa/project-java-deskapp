package com.expensemanager.view;

import com.expensemanager.FlatLaf;
import com.expensemanager.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main window. Menu: Dashboard, Transactions, Budget, Analytics. Theme toggle top-right. Section 2, 5.
 * Sidebar styled as navigation panel (active: background highlight + bold). Card wrappers for main/card backgrounds.
 */
public class MainFrame extends JFrame {
    private static final int W = 1200;
    private static final int H = 750;
    private static final String[] NAV_CARDS = {"Dashboard", "Transactions", "Budget", "Analytics"};

    private final JPanel cards;
    private final CardLayout cardLayout;
    private final DashboardView dashboardView;
    private final TransactionsView transactionsView;
    private final BudgetView budgetView;
    private final AnalyticsView analyticsView;
    private final List<JPanel> cardWrappers = new ArrayList<>();
    private final List<JButton> navButtons = new ArrayList<>();
    private String currentCard = "Dashboard";
    private JPanel northPanel;
    private JPanel westPanel;
    private JPanel westWrap;

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

        cards.add(createCardWrapper(dashboardView), "Dashboard");
        cards.add(createCardWrapper(transactionsView), "Transactions");
        cards.add(createCardWrapper(budgetView), "Budget");
        cards.add(createCardWrapper(analyticsView), "Analytics");

        northPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Personal Expense Manager");
        title.setFont(title.getFont().deriveFont(18f));
        northPanel.add(title, BorderLayout.WEST);

        JButton themeBtn = new JButton(FlatLaf.isDark() ? "☀ Light" : "🌙 Dark");
        themeBtn.addActionListener(e -> {
            FlatLaf.toggleTheme();
            themeBtn.setText(FlatLaf.isDark() ? "☀ Light" : "🌙 Dark");
        });
        northPanel.add(themeBtn, BorderLayout.EAST);
        add(northPanel, BorderLayout.NORTH);

        westPanel = new JPanel(new GridLayout(4, 1, 0, 4));
        westPanel.add(createNavButton("📊 Dashboard", "Dashboard"));
        westPanel.add(createNavButton("📝 Transactions", "Transactions"));
        westPanel.add(createNavButton("💰 Budget", "Budget"));
        westPanel.add(createNavButton("📈 Analytics", "Analytics"));

        westWrap = new JPanel(new BorderLayout());
        westWrap.setPreferredSize(new Dimension(220, 0));
        westWrap.add(westPanel, BorderLayout.NORTH);
        add(westWrap, BorderLayout.WEST);
        add(cards, BorderLayout.CENTER);

        refreshTheme();
        showCard("Dashboard");
    }

    private JPanel createCardWrapper(JComponent view) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(view, BorderLayout.CENTER);
        cardWrappers.add(p);
        return p;
    }

    private JButton createNavButton(String label, String card) {
        JButton b = new JButton(label);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.addActionListener(e -> showCard(card));
        navButtons.add(b);
        return b;
    }

    private void updateSidebarSelection(String active) {
        boolean dark = FlatLaf.isDark();
        for (int i = 0; i < navButtons.size() && i < NAV_CARDS.length; i++) {
            boolean selected = NAV_CARDS[i].equals(active);
            JButton btn = navButtons.get(i);
            btn.setBackground(selected ? UIUtils.getSidebarActiveBackground(dark) : UIUtils.getSidebarBackground(dark));
            btn.setFont(btn.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN));
        }
    }

    /** Called when theme is toggled to re-apply backgrounds and sidebar styles. */
    public void refreshTheme() {
        boolean dark = FlatLaf.isDark();
        northPanel.setBackground(UIUtils.getSidebarBackground(dark));
        westPanel.setBackground(UIUtils.getSidebarBackground(dark));
        westWrap.setBackground(UIUtils.getSidebarBackground(dark));
        westPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        westWrap.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.getSidebarBorderColor(dark)));
        cards.setBackground(UIUtils.getMainBackground(dark));
        for (JPanel w : cardWrappers) {
            w.setBackground(UIUtils.getMainBackground(dark));
            if (w.getComponentCount() > 0) {
                w.getComponent(0).setBackground(UIUtils.getCardBackground(dark));
            }
        }
        updateSidebarSelection(currentCard);
    }

    public void showCard(String name) {
        currentCard = name;
        cardLayout.show(cards, name);
        updateSidebarSelection(name);
        dashboardView.onShown();
        transactionsView.onShown();
        budgetView.onShown();
        analyticsView.onShown();
    }

    public void refreshDashboard() { dashboardView.refresh(); }
    public void refreshTransactions() { transactionsView.refresh(); }
    public void refreshBudget() { budgetView.refresh(); }
}
