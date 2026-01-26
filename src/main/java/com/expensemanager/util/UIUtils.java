package com.expensemanager.util;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Centralized UI theme config per Section 5 (Theme &amp; UI rules).
 * Applies: global font (Segoe UI / Inter, 14pt), Primary #4F46E5, backgrounds,
 * FlatLaf arcs (12), no focus border, and color constants for sidebar/cards.
 */
public final class UIUtils {

    /** Primary color - Blue/Indigo per spec. */
    public static final int COLOR_PRIMARY = 0x4F46E5;

    /** Light: main panel background. */
    public static final int COLOR_MAIN_BG_LIGHT = 0xF3F4F6;
    /** Light: content cards. */
    public static final int COLOR_CARD_BG_LIGHT = 0xFFFFFF;
    /** Light: sidebar background. */
    public static final int COLOR_SIDEBAR_BG_LIGHT = 0xFFFFFF;
    /** Light: sidebar active item highlight. */
    public static final int COLOR_SIDEBAR_ACTIVE_LIGHT = 0xE0E7FF;
    /** Light: sidebar border. */
    public static final int COLOR_SIDEBAR_BORDER_LIGHT = 0xE5E7EB;

    /** Dark: main panel background. */
    public static final int COLOR_MAIN_BG_DARK = 0x1F2937;
    /** Dark: content cards. */
    public static final int COLOR_CARD_BG_DARK = 0x111827;
    /** Dark: sidebar background. */
    public static final int COLOR_SIDEBAR_BG_DARK = 0x1F2937;
    /** Dark: sidebar active item highlight. */
    public static final int COLOR_SIDEBAR_ACTIVE_DARK = 0x4338CA;
    /** Dark: sidebar border. */
    public static final int COLOR_SIDEBAR_BORDER_DARK = 0x374151;

    private static final int ARC = 12;

    private UIUtils() {}

    /**
     * Resolve base font: Segoe UI or Inter, size 14. Called after LAF setup.
     */
    public static Font getBaseFont() {
        String[] tryNames = { "Segoe UI", "Inter" };
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String name : tryNames) {
            if (Arrays.asList(available).contains(name)) {
                return new Font(name, Font.PLAIN, 14);
            }
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    }

    /**
     * Apply FlatLaf overrides: rounded corners (arc=12), no focus border, global font.
     * Call after FlatLightLaf/FlatDarkLaf.setup().
     *
     * @param dark whether dark theme is active (reserved for future per-theme tweaks)
     */
    public static void applyTheme(boolean dark) {
        // Primary JButtons: pill shape (arc 999). ToggleButton: rounded rect (12). Other: 12.
        UIManager.put("Button.arc", 999);
        UIManager.put("ToggleButton.arc", ARC);
        UIManager.put("Component.arc", ARC);
        UIManager.put("TextComponent.arc", ARC);
        UIManager.put("CheckBox.arc", ARC);
        UIManager.put("ProgressBar.arc", ARC);

        // Remove focus borders for a cleaner look
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);

        // Global font: Segoe UI or Inter, 14pt
        Font base = getBaseFont();
        UIManager.put("Button.font", base);
        UIManager.put("Label.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("TextArea.font", base);
        UIManager.put("ComboBox.font", base);
        UIManager.put("Table.font", base);
        UIManager.put("TitledBorder.font", base);
        UIManager.put("List.font", base);
    }

    public static Color getMainBackground(boolean dark) {
        return new Color(dark ? COLOR_MAIN_BG_DARK : COLOR_MAIN_BG_LIGHT);
    }

    public static Color getCardBackground(boolean dark) {
        return new Color(dark ? COLOR_CARD_BG_DARK : COLOR_CARD_BG_LIGHT);
    }

    public static Color getSidebarBackground(boolean dark) {
        return new Color(dark ? COLOR_SIDEBAR_BG_DARK : COLOR_SIDEBAR_BG_LIGHT);
    }

    public static Color getSidebarActiveBackground(boolean dark) {
        return new Color(dark ? COLOR_SIDEBAR_ACTIVE_DARK : COLOR_SIDEBAR_ACTIVE_LIGHT);
    }

    public static Color getSidebarBorderColor(boolean dark) {
        return new Color(dark ? COLOR_SIDEBAR_BORDER_DARK : COLOR_SIDEBAR_BORDER_LIGHT);
    }

    public static Color getPrimary() {
        return new Color(COLOR_PRIMARY);
    }
}
