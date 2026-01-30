package com.expensemanager.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public final class UIFactory {

    public static final int COLOR_PRIMARY_BLUE = 0x2563EB;
    public static final int COLOR_SIDEBAR_TEXT = 0x374151;
    public static final int COLOR_SIDEBAR_HOVER = 0xF3F4F6;

    public static final Insets BUTTON_MARGIN = new Insets(8, 16, 8, 16);
    public static final int SIDEBAR_ARC = 30;

    private UIFactory() {
    }

    public static JButton createPrimaryButton(String text) {
        return createPrimaryButton(text, null);
    }

    public static JButton createPrimaryButton(String text, Icon icon) {
        JButton b = new JButton(text, icon);
        b.setFocusPainted(false);
        b.setBackground(new Color(COLOR_PRIMARY_BLUE));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setMargin(BUTTON_MARGIN);
        return b;
    }

    public static Icon createPlusIcon() {
        int s = 16;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        int t = 2;
        int m = s / 2;
        g.fillRect(m - t / 2, 2, t, s - 4);
        g.fillRect(2, m - t / 2, s - 4, t);
        g.dispose();
        return new ImageIcon(img);
    }

    public static Color getSidebarTextColor() {
        return new Color(COLOR_SIDEBAR_TEXT);
    }

    public static Color getSidebarHoverColor() {
        return new Color(COLOR_SIDEBAR_HOVER);
    }

    public static Color getPrimaryBlue() {
        return new Color(COLOR_PRIMARY_BLUE);
    }
}
