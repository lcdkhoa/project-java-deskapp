package com.expensemanager.view;

import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

/**
 * BarPainter that draws bars with rounded top edges. No shadow.
 * Section 1.3: "Bar bo tròn".
 */
public final class RoundedBarPainter implements BarPainter {

    private static final int ARC = 6;

    @Override
    public void paintBar(Graphics2D g2, BarRenderer renderer, int row, int column,
                        RectangularShape bar, RectangleEdge base) {
        Paint p = renderer.getItemPaint(row, column);
        if (p == null) p = renderer.getDefaultPaint();
        g2.setPaint(p);
        double x = bar.getX();
        double y = bar.getY();
        double w = bar.getWidth();
        double h = bar.getHeight();
        int arc = (int) Math.min(ARC, Math.min(w, h) / 2);
        if (arc < 1) arc = 1;
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, arc, arc));
    }

    @Override
    public void paintBarShadow(Graphics2D g2, BarRenderer renderer, int row, int column,
                               RectangularShape bar, RectangleEdge base, boolean pegShadow) {
        // No shadow for a clean look
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RoundedBarPainter;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
