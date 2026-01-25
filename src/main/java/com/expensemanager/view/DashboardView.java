package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard - Section 1. Month selector, 4 KPI cards, Last 7 Days, By Category, Monthly Cashflow, Budget Warnings, Add Transaction.
 */
public class DashboardView extends JPanel {
    private final MainFrame main;
    private final DashboardController controller;

    public DashboardView(MainFrame main) {
        this.main = main;
        this.controller = new DashboardController(this);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.add(controller.getMonthSelectorPanel(), BorderLayout.CENTER);
        JButton addTx = new JButton("+ Add Transaction");
        addTx.addActionListener(e -> main.openCreateTransaction());
        top.add(addTx, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel kpi = controller.getKpiCardsPanel();
        add(kpi, BorderLayout.CENTER);

        // Placeholder for charts + budget warnings - will be in a scroll or grid
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(controller.getChartsPanel());
        content.add(controller.getBudgetWarningsPanel());
        JScrollPane scroll = new JScrollPane(content);
        add(scroll, BorderLayout.CENTER);

        // Re-layout: we had KPI in center and then overwrote with scroll. Fix: put KPI above scroll.
        removeAll();
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(top, BorderLayout.NORTH);
        JPanel upper = new JPanel(new BorderLayout());
        upper.add(kpi, BorderLayout.NORTH);
        upper.add(scroll, BorderLayout.CENTER);
        add(upper, BorderLayout.CENTER);
    }

    void onShown() { refresh(); }
    void refresh() { controller.refresh(); }

    MainFrame getMain() { return main; }
}
