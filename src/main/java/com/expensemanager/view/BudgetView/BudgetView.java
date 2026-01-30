package com.expensemanager.view.BudgetView;

import com.expensemanager.controller.BudgetController;
import com.expensemanager.view.CommonComponents.MainFrame;
import com.expensemanager.view.CommonComponents.StyledComponents;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class BudgetView extends JPanel {
    private final MainFrame main;
    private final BudgetController controller;

    public BudgetView(MainFrame main) {
        this.main = main;
        this.controller = new BudgetController(this);
        setBackground(Color.WHITE);
        setOpaque(true);
        setLayout(new MigLayout("wrap 1, fill, insets 20 24 24 24, gapy 24", "[grow]", "[][grow]"));

        JPanel header = new JPanel(new MigLayout("ins 0, fillx", "[grow][pref!]", "[]"));
        header.setOpaque(false);

        JPanel left = new JPanel(new MigLayout("ins 0, wrap 2", "[]", "[]2[]"));
        left.setOpaque(false);
        JLabel title = new JLabel("Budget");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        left.add(title, "wrap");
        JLabel subtitle = new JLabel("Manage your monthly budget");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
        subtitle.setForeground(new Color(0x6B7280));
        left.add(subtitle);
        header.add(left, "growx");

        JButton addBudget = StyledComponents.createTitleButton("Add Budget", StyledComponents.createPlusIcon(), 180);
        addBudget.addActionListener(e -> controller.openAddBudget());
        header.add(addBudget, "aligny center");

        add(header, "growx");
        add(controller.getContentPanel(), "grow, push");
    }

    public void onShown() {
        refresh();
    }

    public void refresh() {
        controller.refresh();
    }

    public MainFrame getMain() {
        return main;
    }
}
