package com.expensemanager;

import com.expensemanager.util.AppContext;
import com.expensemanager.view.CommonComponents.MainFrame;
import javax.swing.*;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class App {
    public static void main(String[] args) {
        String userId = "75b2a244-ea7f-4b29-adc2-b9b21c03b383";
        AppContext.setUserId(userId);
        FlatMacLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            MainFrame f = new MainFrame();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
