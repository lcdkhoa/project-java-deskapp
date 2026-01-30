package com.expensemanager.view.DashboardView;

import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.RectangularShape;

public final class RoundedBarPainter implements BarPainter {

    private static final int ARC = 6;

    @Override
    public void paintBar(Graphics2D g2, BarRenderer renderer, int row, int column,
            RectangularShape bar, RectangleEdge base) {
        Paint p = renderer.getItemPaint(row, column);
        if (p == null)
            p = renderer.getDefaultPaint();
        g2.setPaint(p);
        double x = bar.getX();
        double y = bar.getY();
        double w = bar.getWidth();
        double h = bar.getHeight();
        int arc = (int) Math.min(ARC, Math.min(w, h) / 2);
        if (arc < 1)
            arc = 1;

        GeneralPath path = new GeneralPath();
        path.moveTo(x + arc, y);
        path.lineTo(x + w - arc, y);
        path.quadTo(x + w, y, x + w, y + arc);
        path.lineTo(x + w, y + h);
        path.lineTo(x, y + h);
        path.lineTo(x, y + arc);
        path.quadTo(x, y, x + arc, y);
        path.closePath();
        g2.fill(path);
    }

    @Override
    public void paintBarShadow(Graphics2D g2, BarRenderer renderer, int row, int column,
            RectangularShape bar, RectangleEdge base, boolean pegShadow) {
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
