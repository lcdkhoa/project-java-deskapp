package com.expensemanager.view.CommonComponents;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    public static final int DEFAULT_ARC = 30;
    public static final Color DEFAULT_BACKGROUND = Color.WHITE;
    public static final Color DEFAULT_BORDER_COLOR = new Color(0xE5E7EB);

    private int arc;
    private Color backgroundColor;
    private Color borderColor;
    private float borderWidth;
    private boolean drawBorder;

    public RoundedPanel() {
        this(DEFAULT_ARC, DEFAULT_BACKGROUND, DEFAULT_BORDER_COLOR, 1f, true);
    }

    public RoundedPanel(int arc) {
        this(arc, DEFAULT_BACKGROUND, DEFAULT_BORDER_COLOR, 1f, true);
    }

    public RoundedPanel(int arc, Color backgroundColor, Color borderColor, float borderWidth, boolean drawBorder) {
        this.arc = arc;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.drawBorder = drawBorder;

        setOpaque(false);
        setBackground(backgroundColor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        if (drawBorder && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    public int getArc() {
        return arc;
    }

    public void setArc(int arc) {
        this.arc = arc;
        repaint();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        repaint();
    }

    public boolean isDrawBorder() {
        return drawBorder;
    }

    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
        repaint();
    }

    public static RoundedPanel createCard() {
        return new RoundedPanel();
    }

    public static RoundedPanel createNoBorder(int arc, Color backgroundColor) {
        return new RoundedPanel(arc, backgroundColor, null, 0f, false);
    }
}
