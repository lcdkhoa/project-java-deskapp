package com.expensemanager.view.CommonComponents;

import com.expensemanager.view.TransactionView.TransactionView;
import com.expensemanager.view.DashboardView.DashboardView;
import com.expensemanager.view.BudgetView.BudgetView;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private static final int W = 1200;
    private static final int H = 750;

    private final JPanel cards;
    private final CardLayout cardLayout;
    private final DashboardView dashboardView;
    private final TransactionView transactionsView;
    private final BudgetView budgetView;
    private final List<JPanel> cardWrappers = new ArrayList<>();
    private final List<SidebarButton> navButtons = new ArrayList<>();
    private final ButtonGroup navGroup = new ButtonGroup();
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
        transactionsView = new TransactionView(this);
        budgetView = new BudgetView(this);

        cards.add(createCardWrapper(dashboardView), "Dashboard");
        cards.add(createCardWrapper(transactionsView), "Transactions");
        cards.add(createCardWrapper(budgetView), "Budget");

        northPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Expense Manager");
        title.setFont(title.getFont().deriveFont(18f));
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 0));
        northPanel.add(title, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        westPanel = new JPanel(new MigLayout("wrap 1, insets 20 10 20 10, gapy 15", "fill, grow"));
        westPanel.add(createNavButton("Dashboard", "Dashboard"), "h 48!");
        westPanel.add(createNavButton("Transactions", "Transactions"), "h 48!");
        westPanel.add(createNavButton("Budget", "Budget"), "h 48!");

        westWrap = new JPanel(new BorderLayout());
        westWrap.setPreferredSize(new Dimension(220, 0));
        westWrap.add(westPanel, BorderLayout.NORTH);
        add(westWrap, BorderLayout.WEST);
        add(cards, BorderLayout.CENTER);

        Color sidebarBg = new Color(StyledComponents.COLOR_SIDEBAR_BG_LIGHT);
        Color mainBg = new Color(StyledComponents.COLOR_MAIN_BG_LIGHT);
        Color cardBg = new Color(StyledComponents.COLOR_CARD_BG_LIGHT);
        Color sidebarBorder = new Color(StyledComponents.COLOR_SIDEBAR_BORDER_LIGHT);
        northPanel.setBackground(sidebarBg);
        westPanel.setBackground(sidebarBg);
        westWrap.setBackground(sidebarBg);
        westPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        westWrap.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, sidebarBorder));
        cards.setBackground(mainBg);
        for (JPanel w : cardWrappers) {
            w.setBackground(mainBg);
            if (w.getComponentCount() > 0) {
                Component c = w.getComponent(0);
                if (c instanceof JScrollPane) {
                    JScrollPane sp = (JScrollPane) c;
                    sp.getViewport().setBackground(Color.WHITE);
                    sp.getViewport().setOpaque(true);
                    if (sp.getViewport().getView() instanceof DashboardView) {
                        ((DashboardView) sp.getViewport().getView()).setBackground(mainBg);
                    }
                } else {
                    c.setBackground(c instanceof DashboardView ? mainBg : cardBg);
                }
            }
        }

        showCard("Dashboard");
    }

    private JPanel createCardWrapper(JComponent view) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

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
        String iconPath = "src/main/java/com/expensemanager/img/menu/" + getIconFileName(card);
        ImageIcon icon = StyledComponents.getIcon(iconPath, 20, 20);

        SidebarButton b = new SidebarButton(card, label, icon);
        b.addActionListener(e -> showCard(card));
        navGroup.add(b);
        navButtons.add(b);
        return b;
    }

    private String getIconFileName(String card) {
        switch (card) {
            case "Dashboard":
                return "dashboard.png";
            case "Transactions":
                return "transactions.png";
            case "Budget":
                return "budget.png";
            default:
                return null;
        }
    }

    private void updateSidebarSelection(String active) {
        for (SidebarButton btn : navButtons) {
            btn.setSelected(btn.getCardName().equals(active));
        }
    }

    public void showCard(String name) {
        cardLayout.show(cards, name);
        updateSidebarSelection(name);
        dashboardView.onShown();
        transactionsView.onShown();
        budgetView.onShown();
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
