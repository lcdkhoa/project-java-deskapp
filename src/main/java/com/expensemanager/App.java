package com.expensemanager;

import com.expensemanager.view.CommonComponents.MainFrame;
import javax.swing.*;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class App {
    public static void main(String[] args) {
        FlatMacLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
