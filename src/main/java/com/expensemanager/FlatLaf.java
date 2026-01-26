package com.expensemanager;

import com.expensemanager.util.UIUtils;
import com.expensemanager.view.MainFrame;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * FlatLaf setup. Default: Light. Toggle to Dark - Section 5.
 * Primary: Blue/Indigo. Uses FlatMacLightLaf / FlatMacDarkLaf; UIUtils applies
 * font, arcs, no focus border, and color constants per Section 5.
 */
public final class FlatLaf {
    private static boolean dark = false;

    public static void setup() {
        FlatMacLightLaf.setup();
        UIUtils.applyTheme(false);
    }

    public static boolean isDark() { return dark; }

    public static void toggleTheme() {
        dark = !dark;
        if (dark) {
            FlatMacDarkLaf.setup();
        } else {
            FlatMacLightLaf.setup();
        }
        UIUtils.applyTheme(dark);
        for (Frame f : Frame.getFrames()) {
            SwingUtilities.updateComponentTreeUI(f);
            if (f instanceof MainFrame) {
                ((MainFrame) f).refreshTheme();
            }
        }
    }
}
