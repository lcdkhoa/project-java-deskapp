package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * Material-style card: white background, 16px rounded corners, 20px padding,
 * subtle drop shadow. Use as a container for KPI, charts, or other content.
 */
public class ModernCard extends JPanel {

    private static final int RADIUS = 16;
    private static final int SHADOW_OFFSET = 2;
    private static final int PADDING = 20;
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 28);

    public ModernCard() {
        this(null);
    }

    /**
     * @param content component to show inside the card; may be null (use add() later)
     */
    public ModernCard(Component content) {
        setOpaque(true);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        if (content != null) {
            add(content, BorderLayout.CENTER);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int cw = Math.max(0, w - SHADOW_OFFSET);
        int ch = Math.max(0, h - SHADOW_OFFSET);

        // Subtle drop shadow
        g2.setColor(SHADOW_COLOR);
        g2.fillRoundRect(SHADOW_OFFSET, SHADOW_OFFSET, cw, ch, RADIUS + 2, RADIUS + 2);

        // White card, 16px radius
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, cw, ch, RADIUS, RADIUS);

        g2.dispose();
    }
}
