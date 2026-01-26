package com.expensemanager.view;

import com.expensemanager.util.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * KPI card with custom icons from imgs/dashboard/. MigLayout: Row 1 (Icon + Title), Row 2 (Value).
 * Expense: red; Income: green; Remaining: blue; Budget Used: purple.
 */
public class KPICard extends ModernCard {

    private static final Color TITLE_COLOR = new Color(0x6B7280);
    private static final int ICON_SIZE = 32;
    private static final int TITLE_FONT_SIZE = 20;
    private static final int VALUE_FONT_SIZE = 32;

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel iconLabel;

    /**
     * @param title      Gray, small (e.g. "Monthly Expense")
     * @param valueText  Large bold, colored (e.g. "16,435,000" or "85%")
     * @param iconPath   Path to icon image (e.g., "imgs/dashboard/down.png")
     * @param valueColor Value text: #EF4444 expense, #10B981 income, #3B82F6 remaining, purple budget
     */
    public KPICard(String title, String valueText, String iconPath, Color valueColor) {
        super(null);
        // Override ModernCard's border padding from 20 to 15 for more compact card
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        // Override ModernCard's BorderLayout with MigLayout: wrap 1 for strict vertical stacking
        // gapy 10 for gap between title and value rows
        setLayout(new MigLayout("ins 0, wrap 1, gapy 10", "[]", "[]"));

        // Row 1 (Title Row): Icon + Title - same row, vertically centered
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

        // Row 2 (Value Row): Big Value Number (32px Bold) - strictly below title, no text wrapping
        valueLabel = new JLabel(valueText);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, VALUE_FONT_SIZE)); // 32px Bold
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
