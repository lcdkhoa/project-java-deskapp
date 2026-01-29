package com.expensemanager.view;

import com.expensemanager.util.UIUtils;

import javax.swing.*;
import java.awt.*;

public final class CircleIconPanel extends JComponent {

    private static final int DIAMETER = 44;
    private static final int ICON_SIZE = 24;

    private final ImageIcon icon;

    public CircleIconPanel(String iconPath) {
        setOpaque(false);
        setPreferredSize(new Dimension(DIAMETER, DIAMETER));
        setMinimumSize(new Dimension(DIAMETER, DIAMETER));
        setMaximumSize(new Dimension(DIAMETER, DIAMETER));
        this.icon = loadIcon(iconPath);
    }

    private ImageIcon loadIcon(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        return UIUtils.getIcon(path, ICON_SIZE, ICON_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
