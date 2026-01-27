package com.expensemanager.view;

import com.expensemanager.util.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Circular icon background (44x44) used in Transaction rows.
 * Fills a solid colored circle and centers a 24x24 icon inside.
 */
public class CircleIconPanel extends JComponent {

    private static final int DIAMETER = 44;
    private static final int ICON_SIZE = 24;

    private Color backgroundColor;
    private String iconPath;
    private ImageIcon icon;

    public CircleIconPanel(Color backgroundColor, String iconPath) {
        this.backgroundColor = backgroundColor != null ? backgroundColor : new Color(0x9CA3AF);
        this.iconPath = iconPath;
        setOpaque(false);
        setPreferredSize(new Dimension(DIAMETER, DIAMETER));
        setMinimumSize(new Dimension(DIAMETER, DIAMETER));
        setMaximumSize(new Dimension(DIAMETER, DIAMETER));
        loadIcon();
    }

    private void loadIcon() {
        if (iconPath == null || iconPath.isBlank()) {
            icon = null;
            return;
        }
        icon = UIUtils.getIcon(iconPath, ICON_SIZE, ICON_SIZE);
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color != null ? color : new Color(0x9CA3AF);
        repaint();
    }

    public void setIconPath(String path) {
        this.iconPath = path;
        loadIcon();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight());
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        g2.setColor(backgroundColor);
        g2.fillOval(x, y, size, size);

        if (icon != null) {
            int iw = icon.getIconWidth();
            int ih = icon.getIconHeight();
            int ix = (getWidth() - iw) / 2;
            int iy = (getHeight() - ih) / 2;
            g2.drawImage(icon.getImage(), ix, iy, iw, ih, null);
        }

        g2.dispose();
    }
}

