package com.expensemanager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * FlatLaf setup. Default: Light. Toggle to Dark - Section 5.
 * Primary: Blue/Indigo. We use FlatMacLightLaf / FlatMacDarkLaf for a modern look.
 */
public final class FlatLaf {
    private static boolean dark = false;

    public static void setup() {
        FlatMacLightLaf.setup();
    }

    public static boolean isDark() { return dark; }

    public static void toggleTheme() {
        dark = !dark;
        if (dark) {
            FlatMacDarkLaf.setup();
        } else {
            FlatMacLightLaf.setup();
        }
        for (Frame f : Frame.getFrames()) {
            SwingUtilities.updateComponentTreeUI(f);
        }
    }
}
