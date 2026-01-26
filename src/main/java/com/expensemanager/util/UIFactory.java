package com.expensemanager.util;

import com.expensemanager.FlatLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * UI Factory for creating consistently styled buttons matching the Figma design.
 * Blue Theme (#2563EB), pill shape for primary, rounded rect for sidebar.
 */
public final class UIFactory {

    /** Primary blue per design. */
    public static final int COLOR_PRIMARY_BLUE = 0x2563EB;
    /** Unselected sidebar text (dark gray). */
    public static final int COLOR_SIDEBAR_TEXT = 0x374151;
    /** Hover background for unselected sidebar. */
    public static final int COLOR_SIDEBAR_HOVER = 0xF3F4F6;
    /** Dark theme: unselected text. */
    public static final int COLOR_SIDEBAR_TEXT_DARK = 0x9CA3AF;
    /** Dark theme: hover. */
    public static final int COLOR_SIDEBAR_HOVER_DARK = 0x374151;

    /** Padding: top, left, bottom, right. */
    public static final Insets BUTTON_MARGIN = new Insets(8, 16, 8, 16);
    /** Sidebar arc (rounded rect, 30px). */
    public static final int SIDEBAR_ARC = 30;

    private UIFactory() {}

    /**
     * Creates a Primary Action button (e.g. "+ Add Transaction").
     * Pill shape (arc 999), background #2563EB, foreground white, bold 14, no focus paint.
     *
     * @param text button label
     * @return styled JButton
     */
    public static JButton createPrimaryButton(String text) {
        return createPrimaryButton(text, null);
    }

    /**
     * Creates a Primary Action button with optional leading icon.
     * Pill shape, #2563EB, white text, bold 14, margin 8,16,8,16.
     *
     * @param text button label
     * @param icon optional leading icon (e.g. plus); may be null
     * @return styled JButton
     */
    public static JButton createPrimaryButton(String text, Icon icon) {
        JButton b = new JButton(text, icon);
        b.setFocusPainted(false);
        b.setBackground(new Color(COLOR_PRIMARY_BLUE));
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setMargin(BUTTON_MARGIN);
        // Pill shape: use UIManager Button.arc=999 (set in UIUtils). FlatLaf.style does not support "arc".
        return b;
    }

    /**
     * Creates a simple plus icon for primary buttons (e.g. Add Transaction).
     * Size 16x16, white plus on transparent, for use on blue background.
     *
     * @return icon sized for primary button
     */
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

    /**
     * Resolves sidebar text color for unselected state.
     */
    public static Color getSidebarTextColor() {
        return new Color(FlatLaf.isDark() ? COLOR_SIDEBAR_TEXT_DARK : COLOR_SIDEBAR_TEXT);
    }

    /**
     * Resolves sidebar hover background for unselected state.
     */
    public static Color getSidebarHoverColor() {
        return new Color(FlatLaf.isDark() ? COLOR_SIDEBAR_HOVER_DARK : COLOR_SIDEBAR_HOVER);
    }

    /**
     * Resolves primary blue (used for selected state).
     */
    public static Color getPrimaryBlue() {
        return new Color(COLOR_PRIMARY_BLUE);
    }
}
