package com.expensemanager.view;

import com.expensemanager.util.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Budget summary card: same structure as KPICard (Dashboard).
 * Row 1 = Icon + Title, Row 2 = Value. White bg, arc 30, 1px border #E5E7EB.
 */
public class BudgetCard extends ModernCard {

    private static final Color TITLE_COLOR = new Color(0x6B7280);
    private static final int ICON_SIZE = 16;
    private static final int TITLE_FONT_SIZE = 20;
    private static final int VALUE_FONT_SIZE = 32;
    private static final int CARD_HEIGHT = 125;

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel iconLabel;

    /**
     * Same contract as KPICard: title (gray), value (colored), icon path, value color.
     */
    public BudgetCard(String title, String valueText, String iconPath, Color valueColor) {
        super(null);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setLayout(new MigLayout("ins 0, wrap 1, gapy 10", "[]", "[]"));

        setPreferredSize(new Dimension(0, CARD_HEIGHT));
        setMinimumSize(new Dimension(0, CARD_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        // Row 1: Icon + Title (same row, left-aligned, vertically centered)
        JPanel row1 = new JPanel(new MigLayout("ins 0, gap 8 0", "[][]", "[center]"));
        row1.setOpaque(false);

        iconLabel = new JLabel();
        ImageIcon icon = UIUtils.getIcon(iconPath, ICON_SIZE, ICON_SIZE);
        if (icon != null) {
            iconLabel.setIcon(icon);
        } else {
            iconLabel.setText("?");
            iconLabel.setFont(iconLabel.getFont().deriveFont(Font.BOLD, 16f));
        }
        row1.add(iconLabel, "align left, aligny center");

        titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, TITLE_FONT_SIZE));
        titleLabel.setForeground(TITLE_COLOR);
        row1.add(titleLabel, "align left, aligny center");
        add(row1, "wrap");

        // Row 2: Value (large, colored, left-aligned)
        valueLabel = new JLabel(valueText);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, VALUE_FONT_SIZE));
        valueLabel.setForeground(valueColor != null ? valueColor : new Color(0x111827));
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        valueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        add(valueLabel, "align left");
    }

    public void setAmount(String text, Color valueColor) {
        valueLabel.setText(text);
        valueLabel.setForeground(valueColor != null ? valueColor : new Color(0x111827));
    }
}
