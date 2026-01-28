package com.expensemanager.view;

import net.miginfocom.swing.MigLayout;

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
        setBackground(Color.WHITE);
        setOpaque(true);
        setLayout(new MigLayout("wrap 1, fill, insets 20 24 24 24, gapy 24", "[grow]", "[][grow]"));

        // Header row: title/subtitle + Add Budget button
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

        JButton addBudget = createAddBudgetButton();
        addBudget.addActionListener(e -> controller.openAddBudget());
        header.add(addBudget, "aligny center");

        add(header, "growx");
        add(controller.getContentPanel(), "grow, push");
    }

    void onShown() { refresh(); }
    void refresh() { controller.refresh(); }

    MainFrame getMain() { return main; }

    private JButton createAddBudgetButton() {
        JButton btn = new JButton("+ Add Budget") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0x2563EB));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(Color.WHITE);
                Font font = getFont().deriveFont(Font.BOLD, 14f);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics(font);
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(140, 48));
        return btn;
    }
}
