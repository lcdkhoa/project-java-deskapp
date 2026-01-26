package com.expensemanager;

import com.expensemanager.util.UIUtils;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

/**
 * FlatLaf setup. Light mode only (FlatMacLightLaf). No dark/light toggle.
 */
public final class FlatLaf {

    public static void setup() {
        FlatMacLightLaf.setup();
        UIUtils.applyTheme(false);
    }

    /** @deprecated Light mode only; always returns false. Kept for compatibility. */
    public static boolean isDark() { return false; }
}
