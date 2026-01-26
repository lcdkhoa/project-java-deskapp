package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * Card: white background, arc 20 rounded corners, 1px border #E5E7EB, 20px internal padding.
 * Use for KPI, charts. FlatLaf.style arc: 20; custom paint for rounded rect.
 */
public class ModernCard extends JPanel {

    private static final int PADDING = 20;
    private static final int ARC = 30;
    private static final Color BORDER_COLOR = new Color(229, 231, 235); // #E5E7EB

    public ModernCard() {
        this(null);
    }

    /**
     * @param content component to show inside the card; may be null (use add() later)
     */
    public ModernCard(Component content) {
        setOpaque(false);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        putClientProperty("FlatLaf.style", "arc: " + ARC);
        if (content != null) {
            add(content, BorderLayout.CENTER);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        g2.dispose();
    }
}
