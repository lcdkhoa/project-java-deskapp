package com.expensemanager.view.DashboardView;

import com.expensemanager.view.CommonComponents.ModernCard;

import com.expensemanager.util.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class KPICard extends ModernCard {

    private static final Color TITLE_COLOR = new Color(0x6B7280);
    private static final int ICON_SIZE = 30;
    private static final int TITLE_FONT_SIZE = 20;
    private static final int VALUE_FONT_SIZE = 32;

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel iconLabel;

    public KPICard(String title, String valueText, String iconPath, Color valueColor) {
        super(null);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setLayout(new MigLayout("ins 0, wrap 1, gapy 10", "[]", "[]"));

        // Icon + Title
        JPanel row1 = new JPanel(new MigLayout("ins 0, gap 8 0", "[][]", "[center]"));
        row1.setOpaque(false);

        iconLabel = new JLabel();
        ImageIcon icon = UIUtils.getIcon(iconPath, ICON_SIZE, ICON_SIZE);
        if (icon != null) {
            iconLabel.setIcon(icon);
        } else {
            // Fallback: use a placeholder
            iconLabel.setText("?");
            iconLabel.setFont(iconLabel.getFont().deriveFont(Font.BOLD, 16f));
        }
        row1.add(iconLabel, "align left, aligny center");

        titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, TITLE_FONT_SIZE)); // 20px Regular/Medium
        titleLabel.setForeground(TITLE_COLOR);
        row1.add(titleLabel, "align left, aligny center");
        add(row1, "wrap");

        // Row 2 (Value Row): Big Value Number (32px Plain) - strictly below title, no
        // text wrapping
        valueLabel = new JLabel(valueText);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, VALUE_FONT_SIZE)); // 32px Plain (not bold)
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        // Prevent text wrapping - expand card width if necessary
        valueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        add(valueLabel, "align left");
    }

    public void setAmount(String text, Color valueColor) {
        valueLabel.setText(text);
        valueLabel.setForeground(valueColor);
    }
}
