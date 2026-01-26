package com.expensemanager.view;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Budget warnings: light red bg (#FEF2F2), red border (#FECACA), arc 30, warning icon from imgs/dashboard/warn.png.
 * Card-in-card style: each warning item is a white card (arc 30, 85px height) inside the red panel.
 */
public class BudgetWarningsPanel extends JPanel {

    private static final Color BG = new Color(254, 242, 242); // #FEF2F2
    private static final Color BORDER = new Color(0xFECACA); // Red border
    private static final Color TITLE_RED = new Color(0x991B1B);
    private static final Color PERCENT_RED = new Color(0xEF4444);
    private static final Color AMOUNT_GRAY = new Color(0x6B7280); // Gray for amount text
    private static final int RADIUS = 30;
    private static final int WARNING_CARD_HEIGHT = 85;

    private final JPanel listPanel;

    public BudgetWarningsPanel() {
        setOpaque(false);
        setBackground(BG);
        setLayout(new BorderLayout(12, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        putClientProperty("FlatLaf.style", "arc: " + RADIUS);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);
        JLabel iconLbl = new JLabel();
        ImageIcon warnIcon = UIUtils.getIcon("imgs/dashboard/warn.png", 20, 20);
        if (warnIcon != null) {
            iconLbl.setIcon(warnIcon);
        } else {
            iconLbl.setText("!");
            iconLbl.setFont(iconLbl.getFont().deriveFont(Font.BOLD, 18f));
            iconLbl.setForeground(PERCENT_RED);
        }
        header.add(iconLbl);
        JLabel titleLbl = new JLabel("Budget Warnings");
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 14f));
        titleLbl.setForeground(TITLE_RED);
        header.add(titleLbl);
        add(header, BorderLayout.NORTH);

        listPanel = new JPanel(new MigLayout("wrap 1, fillx, gapy 10", "[grow,fill]", "[]"));
        listPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Update the list of over-budget items. Each item is a white card (arc 30, 85px height).
     * Call setVisible(!over.isEmpty()) from the controller.
     */
    public void setWarnings(List<BudgetDAO.BudgetUsedRow> over, Map<String, String> idToName) {
        listPanel.removeAll();
        for (BudgetDAO.BudgetUsedRow r : over) {
            String name = idToName.getOrDefault(r.categoryId, r.categoryId);
            String amountStr = CurrencyUtil.format(r.spent) + " / " + CurrencyUtil.format(r.budget);
            String pctStr = String.format("%.0f%%", r.percentUsed);

            // White card for each warning item
            JPanel warningCard = new WarningItemCard(name, amountStr, pctStr);
            listPanel.add(warningCard, "growx, h " + WARNING_CARD_HEIGHT + "!");
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    /** White card for a single warning item: arc 30, fixed height 85px. */
    private static final class WarningItemCard extends JPanel {
        WarningItemCard(String categoryName, String amountStr, String pctStr) {
            setOpaque(false);
            setBackground(Color.WHITE);
            // Layout: fillx, wrap 2 for two columns, []0[] for no gap between rows
            setLayout(new MigLayout("ins 15, fillx, wrap 2", "[grow][]", "[]0[]"));
            putClientProperty("FlatLaf.style", "arc: 30");

            // Row 1: Category Name (21px PLAIN, Black) | Percentage (21px PLAIN, Red) - same baseline
            JLabel categoryLabel = new JLabel(categoryName);
            categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.PLAIN, 21f));
            categoryLabel.setForeground(Color.BLACK);
            add(categoryLabel, "cell 0 0, align left, aligny center");

            JLabel pctLabel = new JLabel(pctStr);
            pctLabel.setFont(pctLabel.getFont().deriveFont(Font.PLAIN, 21f));
            pctLabel.setForeground(PERCENT_RED);
            pctLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            add(pctLabel, "cell 1 0, align right, aligny center");

            // Row 2: Amount Text (14px Regular, Gray) - directly below Category Name
            JLabel amountLabel = new JLabel(amountStr);
            amountLabel.setFont(amountLabel.getFont().deriveFont(Font.PLAIN, 14f));
            amountLabel.setForeground(AMOUNT_GRAY);
            add(amountLabel, "cell 0 1, align left");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            g2.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
        g2.dispose();
    }
}
