package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * KPI card extending ModernCard. Section 1.2: 40x40 rounded icon (left), title + value (right).
 * Expense: red; Income: green; Remaining: blue; Budget Used: purple.
 * Icons mocked with colored circle and symbol (↓, ↑, ◆, %).
 */
public class KPICard extends ModernCard {

    private static final Color TITLE_COLOR = new Color(0x6B7280);
    private static final int ICON_SIZE = 40;
    private static final int VALUE_FONT_SIZE = 20;

    private final JLabel titleLabel;
    private final JLabel valueLabel;

    /**
     * @param title      Gray, small (e.g. "Monthly Expense")
     * @param valueText  Large bold, colored (e.g. "16,435,000" or "85%")
     * @param iconChar   Symbol in 40x40: "↓" expense, "↑" income, "◆" remaining, "%" budget
     * @param iconColor  Background of 40x40: red/green/blue/purple
     * @param valueColor Value text: #EF4444 expense, #10B981 income, #3B82F6 remaining, purple budget
     */
    public KPICard(String title, String valueText, String iconChar, Color iconColor, Color valueColor) {
        super(null);
        setLayout(new BorderLayout(12, 0));

        add(new IconCircle(iconChar, iconColor), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout(0, 2));
        right.setOpaque(false);
        titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(11f));
        titleLabel.setForeground(TITLE_COLOR);
        right.add(titleLabel, BorderLayout.NORTH);
        valueLabel = new JLabel(valueText);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, VALUE_FONT_SIZE));
        valueLabel.setForeground(valueColor);
        right.add(valueLabel, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);
    }

    public void setAmount(String text, Color valueColor) {
        valueLabel.setText(text);
        valueLabel.setForeground(valueColor);
    }

    /** 40x40 rounded (circle) icon with colored background and white symbol. */
    private static final class IconCircle extends JPanel {
        private final String symbol;
        private final Color bgColor;

        IconCircle(String symbol, Color bgColor) {
            this.symbol = symbol != null && !symbol.isEmpty() ? symbol : "?";
            this.bgColor = bgColor;
            setOpaque(false);
            setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
            setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillOval(0, 0, ICON_SIZE, ICON_SIZE);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont().deriveFont(Font.BOLD, 18f));
            FontMetrics fm = g2.getFontMetrics();
            int x = (ICON_SIZE - fm.stringWidth(symbol)) / 2;
            int y = (ICON_SIZE - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(symbol, x, y);
            g2.dispose();
        }
    }
}
