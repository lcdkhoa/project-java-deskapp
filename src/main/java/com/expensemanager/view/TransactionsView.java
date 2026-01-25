package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * Transactions - Section 2. Search & Filter, Transaction list, Add Transaction.
 */
public class TransactionsView extends JPanel {
    private final MainFrame main;
    private final TransactionsController controller;

    public TransactionsView(MainFrame main) {
        this.main = main;
        this.controller = new TransactionsController(this);
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.add(controller.getFilterPanel(), BorderLayout.CENTER);
        JButton addTx = new JButton("+ Add Transaction");
        addTx.addActionListener(e -> main.openCreateTransaction());
        top.add(addTx, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        add(controller.getTableScroll(), BorderLayout.CENTER);
    }

    void onShown() { refresh(); }
    void refresh() { controller.refresh(); }
}
