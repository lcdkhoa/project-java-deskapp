package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * Custom progress bar for Section 3.3: 8px height, rounded ends,
 * color by %: &lt;80% Green/Blue, 80-99% Yellow/Orange, &gt;=100% Red.
 */
public class BudgetProgressBar extends JComponent {

    private static final int HEIGHT = 8;
    private static final Color TRACK = new Color(0xE5E7EB);
    private static final Color NORMAL = new Color(0x22C55E);   // Green (< 80%)
    private static final Color WARNING = new Color(0xF59E0B);  // Yellow/Orange (80-99%)
    private static final Color EXCEEDED = new Color(0xB91C1C); // Red (>= 100%)

    private double percent = 0;

    public BudgetProgressBar() {
        setPreferredSize(new Dimension(100, HEIGHT));
        setMaximumSize(new Dimension(Short.MAX_VALUE, HEIGHT));
        setMinimumSize(new Dimension(0, HEIGHT));
    }

    public void setPercent(double percent) {
        this.percent = Math.max(0, percent);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int y = (h - HEIGHT) / 2;
        int th = Math.min(HEIGHT, h);
        int arc = th;

        // Track (background)
        g2.setColor(TRACK);
        g2.fillRoundRect(0, y, w, th, arc, arc);

        // Fill
        double ratio = Math.min(1.0, percent / 100.0);
        int fw = (int) Math.round(ratio * w);
        if (fw > 0) {
            g2.setColor(percent < 80 ? NORMAL : percent < 100 ? WARNING : EXCEEDED);
            g2.fillRoundRect(0, y, fw, th, arc, arc);
        }
        g2.dispose();
    }
}
