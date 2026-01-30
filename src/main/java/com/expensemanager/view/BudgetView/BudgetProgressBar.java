package com.expensemanager.view.BudgetView;

import javax.swing.*;
import java.awt.*;

public class BudgetProgressBar extends JComponent {

    private static final int DEFAULT_HEIGHT = 8;
    private static final int DEFAULT_ARC = 8;
    private static final Color TRACK = new Color(0xE5E7EB);
    private static final Color NORMAL = new Color(0x22C55E);
    private static final Color WARNING = new Color(0xF59E0B);
    private static final Color EXCEEDED = new Color(0xB91C1C);

    private double percent = 0;
    private int barHeight = DEFAULT_HEIGHT;
    private int barArc = DEFAULT_ARC;

    public BudgetProgressBar() {
        updateSize();
    }

    public BudgetProgressBar(int height, int arc) {
        this.barHeight = height;
        this.barArc = arc;
        updateSize();
    }

    private void updateSize() {
        setPreferredSize(new Dimension(100, barHeight));
        setMaximumSize(new Dimension(Short.MAX_VALUE, barHeight));
        setMinimumSize(new Dimension(0, barHeight));
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
        int y = (h - barHeight) / 2;
        int th = Math.min(barHeight, h);
        int arc = Math.min(barArc, th);
        if (arc < 1)
            arc = 1;

        g2.setColor(TRACK);
        g2.fillRoundRect(0, y, w, th, arc, arc);

        double ratio = Math.min(1.0, percent / 100.0);
        int fw = (int) Math.round(ratio * w);
        if (fw > 0) {
            g2.setColor(percent < 80 ? NORMAL : percent < 100 ? WARNING : EXCEEDED);
            g2.fillRoundRect(0, y, fw, th, arc, arc);
        }
        g2.dispose();
    }
}
