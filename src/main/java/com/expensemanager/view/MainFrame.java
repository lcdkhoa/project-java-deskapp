package com.expensemanager.view;

import com.expensemanager.util.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main window. Menu: Dashboard, Transactions, Budget, Analytics. Light mode
 * only.
 * Sidebar uses SidebarButton (active: #2563EB; inactive: transparent; hover:
 * #F3F4F6).
 */
public class MainFrame extends JFrame {
    private static final int W = 1200;
    private static final int H = 750;
    private static final String[] NAV_CARDS = { "Dashboard", "Transactions", "Budget", "Analytics" };

    private final JPanel cards;
    private final CardLayout cardLayout;
    private final DashboardView dashboardView;
    private final TransactionsView transactionsView;
    private final BudgetView budgetView;
    private final AnalyticsView analyticsView;
    private final List<JPanel> cardWrappers = new ArrayList<>();
    private final List<SidebarButton> navButtons = new ArrayList<>();
    private final ButtonGroup navGroup = new ButtonGroup();
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
        JLabel title = new JLabel("Expense Manager");
        title.setFont(title.getFont().deriveFont(18f));
        northPanel.add(title, BorderLayout.WEST);
        add(northPanel, BorderLayout.NORTH);

        // MigLayout: wrap 1, insets 20 10 20 10, gapy 15
        westPanel = new JPanel(new MigLayout("wrap 1, insets 20 10 20 10, gapy 15", "fill, grow"));
        westPanel.add(createNavButton("Dashboard", "Dashboard"), "h 48!");
        westPanel.add(createNavButton("Transactions", "Transactions"), "h 48!");
        westPanel.add(createNavButton("Budget", "Budget"), "h 48!");
        westPanel.add(createNavButton("Analytics", "Analytics"), "h 48!");

        westWrap = new JPanel(new BorderLayout());
        // Increase sidebar width so labels are not truncated
        westWrap.setPreferredSize(new Dimension(260, 0));
        westWrap.add(westPanel, BorderLayout.NORTH);
        add(westWrap, BorderLayout.WEST);
        add(cards, BorderLayout.CENTER);

        refreshTheme();
        showCard("Dashboard");
    }

    private JPanel createCardWrapper(JComponent view) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Wrap DashboardView in JScrollPane with specific styling
        if (view instanceof DashboardView) {
            JScrollPane scrollPane = new JScrollPane(view);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.getViewport().setOpaque(true);
            p.add(scrollPane, BorderLayout.CENTER);
        } else {
            p.add(view, BorderLayout.CENTER);
        }

        cardWrappers.add(p);
        return p;
    }

    private SidebarButton createNavButton(String label, String card) {
        // Load icon from imgs/menu/ folder with mapping
        String iconPath = "imgs/menu/" + getIconFileName(card);
        ImageIcon icon = UIUtils.getIcon(iconPath, 20, 20);
        
        SidebarButton b = new SidebarButton(card, label, icon);
        b.addActionListener(e -> showCard(card));
        navGroup.add(b);
        navButtons.add(b);
        return b;
    }
    
    /**
     * Maps card name to icon file name.
     */
    private String getIconFileName(String card) {
        switch (card) {
            case "Dashboard":
                return "dashboard.png";
            case "Transactions":
                return "transactions.png";
            case "Budget":
                return "budget.png";
            case "Analytics":
                return "analytics.png";
            default:
                return null;
        }
    }

    private void updateSidebarSelection(String active) {
        for (SidebarButton btn : navButtons) {
            btn.setSelected(btn.getCardName().equals(active));
        }
    }

    /** Apply light-mode backgrounds and sidebar styles. */
    public void refreshTheme() {
        northPanel.setBackground(UIUtils.getSidebarBackground(false));
        westPanel.setBackground(UIUtils.getSidebarBackground(false));
        westWrap.setBackground(UIUtils.getSidebarBackground(false));
        westPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        westWrap.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.getSidebarBorderColor(false)));
        cards.setBackground(UIUtils.getMainBackground(false));
        for (JPanel w : cardWrappers) {
            w.setBackground(UIUtils.getMainBackground(false));
            if (w.getComponentCount() > 0) {
                Component c = w.getComponent(0);
                if (c instanceof JScrollPane) {
                    // Handle JScrollPane wrapper for DashboardView
                    JScrollPane scrollPane = (JScrollPane) c;
                    scrollPane.getViewport().setBackground(Color.WHITE);
                    scrollPane.getViewport().setOpaque(true);
                    if (scrollPane.getViewport().getView() instanceof DashboardView) {
                        ((DashboardView) scrollPane.getViewport().getView())
                                .setBackground(UIUtils.getMainBackground(false));
                    }
                } else {
                    c.setBackground(c instanceof DashboardView
                            ? UIUtils.getMainBackground(false)
                            : UIUtils.getCardBackground(false));
                }
            }
        }
        updateSidebarSelection(currentCard);
        for (SidebarButton sb : navButtons) {
            sb.refreshTheme();
        }
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

    public void refreshDashboard() {
        dashboardView.refresh();
    }

    public void refreshTransactions() {
        transactionsView.refresh();
    }

    public void refreshBudget() {
        budgetView.refresh();
    }
}
