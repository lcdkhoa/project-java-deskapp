package com.expensemanager.view;

import javax.swing.*;
import java.awt.*;

/**
 * Card: white background, subtle 1px border #E5E7EB, 20px internal padding.
 * No shadow (FlatDropShadowBorder removed per Figma). Use for KPI, charts, etc.
 */
public class ModernCard extends JPanel {

    private static final int PADDING = 20;
    private static final Color BORDER_COLOR = new Color(229, 231, 235); // #E5E7EB

    public ModernCard() {
        this(null);
    }

    /**
     * @param content component to show inside the card; may be null (use add() later)
     */
    public ModernCard(Component content) {
        setOpaque(true);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        if (content != null) {
            add(content, BorderLayout.CENTER);
        }
    }
}
