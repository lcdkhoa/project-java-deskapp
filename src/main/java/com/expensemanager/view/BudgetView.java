package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * Budget - Section 3. Add/Edit Budget, Monthly Overview, Budget by Category.
 */
public class BudgetView extends JPanel {
    private final MainFrame main;
    private final BudgetController controller;

    public BudgetView(MainFrame main) {
        this.main = main;
        this.controller = new BudgetController(this);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JButton("+ Add Budget") {{
            addActionListener(e -> controller.openAddBudget());
        }});
        add(top, BorderLayout.NORTH);
        add(controller.getContentPanel(), BorderLayout.CENTER);
    }

    void onShown() { refresh(); }
    void refresh() { controller.refresh(); }

    MainFrame getMain() { return main; }
}
