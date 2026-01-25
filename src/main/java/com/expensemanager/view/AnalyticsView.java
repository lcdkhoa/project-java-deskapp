package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * Analytics - Section 4. AI Insights, Spending Habits, Category Analysis, Optimization Tips.
 */
public class AnalyticsView extends JPanel {
    private final MainFrame main;
    private final AnalyticsController controller;

    public AnalyticsView(MainFrame main) {
        this.main = main;
        this.controller = new AnalyticsController(this);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(controller.getContentPanel(), BorderLayout.CENTER);
    }

    void onShown() { refresh(); }
    void refresh() { controller.refresh(); }
}
