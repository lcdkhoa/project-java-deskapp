package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * KPI card per Section 1.2: white background, rounded corners (15), slight drop shadow,
 * icon (left, color-coded), gray title, large bold colored amount.
 */
public class KPICard extends JPanel {

    private static final int RADIUS = 15;
    private static final int SHADOW_OFFSET = 3;
    private static final int PADDING = 12;
    private static final Color TITLE_COLOR = new Color(0x6B7280);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 45);

    private final JLabel iconLabel;
    private final JLabel titleLabel;
    private final JLabel amountLabel;

    /**
     * @param title       Gray, small (e.g. "Monthly Expense")
     * @param amountText  Large bold, colored (e.g. "1.234.567 đ")
     * @param icon        Symbol: "↓" expense, "↑" income, "◆" remaining, "%" budget
     * @param iconColor   Red for expense, green for income/positive remaining, etc.
     * @param amountColor Same logic: red/green by type, or primary for budget
     */
    public KPICard(String title, String amountText, String icon, Color iconColor, Color amountColor) {
        setOpaque(true);
        setLayout(new BorderLayout(PADDING, 0));
        setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING + SHADOW_OFFSET, PADDING + SHADOW_OFFSET));

        iconLabel = new JLabel(icon);
        iconLabel.setFont(iconLabel.getFont().deriveFont(22f));
        iconLabel.setForeground(iconColor);
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        add(iconLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout(0, 2));
        right.setOpaque(false);
        titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(11f));
        titleLabel.setForeground(TITLE_COLOR);
        right.add(titleLabel, BorderLayout.NORTH);
        amountLabel = new JLabel(amountText);
        amountLabel.setFont(amountLabel.getFont().deriveFont(Font.BOLD, 18f));
        amountLabel.setForeground(amountColor);
        right.add(amountLabel, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);
    }

    public void setAmount(String text, Color amountColor) {
        amountLabel.setText(text);
        amountLabel.setForeground(amountColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int cw = Math.max(0, w - SHADOW_OFFSET);
        int ch = Math.max(0, h - SHADOW_OFFSET);

        // Slight drop shadow: rounded rect offset bottom-right
        g2.setColor(SHADOW_COLOR);
        g2.fillRoundRect(SHADOW_OFFSET, SHADOW_OFFSET, cw, ch, RADIUS + 2, RADIUS + 2);

        // White card, rounded corners (radius 15)
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, cw, ch, RADIUS, RADIUS);

        g2.dispose();
    }
}
