package com.expensemanager.view.BudgetView;

import com.expensemanager.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;

public class BudgetCategoryCard extends JPanel {

    private static final int RADIUS = 12;
    private static final int ICON_SIZE = 36;
    private static final Color NAME_COLOR = new Color(0x111827);
    private static final Color AMOUNT_COLOR = new Color(0x6B7280);

    public BudgetCategoryCard(String icon, Color iconBgColor, String name, long spent, long budget,
            double percentUsed) {
        setOpaque(false);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JPanel iconWrap = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBgColor != null ? iconBgColor : AMOUNT_COLOR);
                int s = Math.min(getWidth(), getHeight());
                g2.fillOval((getWidth() - s) / 2, (getHeight() - s) / 2, s, s);
                g2.dispose();
            }
        };
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        JLabel iconLbl = new JLabel(icon != null && !icon.isEmpty() ? icon : "•");
        iconLbl.setFont(iconLbl.getFont().deriveFont(16f));
        iconLbl.setForeground(Color.WHITE);
        iconWrap.add(iconLbl);
        top.add(iconWrap);

        JLabel nameLbl = new JLabel(name != null ? name : "");
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 14f));
        nameLbl.setForeground(NAME_COLOR);
        top.add(nameLbl);
        add(top, BorderLayout.NORTH);

        JLabel amountLbl = new JLabel(CurrencyUtil.format(spent) + " / " + CurrencyUtil.format(budget));
        amountLbl.setFont(amountLbl.getFont().deriveFont(12f));
        amountLbl.setForeground(AMOUNT_COLOR);
        amountLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        add(amountLbl, BorderLayout.CENTER);

        BudgetProgressBar bar = new BudgetProgressBar();
        bar.setPercent(percentUsed);
        add(bar, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
        g2.dispose();
    }
}
