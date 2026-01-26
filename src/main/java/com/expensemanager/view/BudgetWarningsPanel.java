package com.expensemanager.view;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Budget warnings: very light red bg, arc 20, 1px border #E5E7EB, warning icon (!),
 * 'Budget Warnings' title, vertical list with category (bold), amount, percentage (right, red).
 * Only shown when categories exceed 100% budget.
 */
public class BudgetWarningsPanel extends JPanel {

    private static final Color BG = new Color(254, 242, 242);
    private static final Color BORDER = new Color(229, 231, 235); // #E5E7EB
    private static final Color ICON_RED = new Color(0xEF4444);
    private static final Color TITLE_RED = new Color(0x991B1B);
    private static final Color PERCENT_RED = new Color(0xEF4444);
    private static final int RADIUS = 20;

    private final JPanel listPanel;

    public BudgetWarningsPanel() {
        setOpaque(false);
        setBackground(BG);
        setLayout(new BorderLayout(12, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        putClientProperty("FlatLaf.style", "arc: 20");

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);
        JLabel iconLbl = new JLabel("!");
        iconLbl.setFont(iconLbl.getFont().deriveFont(Font.BOLD, 18f));
        iconLbl.setForeground(ICON_RED);
        header.add(iconLbl);
        JLabel titleLbl = new JLabel("Budget Warnings");
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 14f));
        titleLbl.setForeground(TITLE_RED);
        header.add(titleLbl);
        add(header, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Update the list of over-budget items. Call setVisible(!over.isEmpty()) from the controller.
     */
    public void setWarnings(List<BudgetDAO.BudgetUsedRow> over, Map<String, String> idToName) {
        listPanel.removeAll();
        for (BudgetDAO.BudgetUsedRow r : over) {
            String name = idToName.getOrDefault(r.categoryId, r.categoryId);
            String amountStr = CurrencyUtil.format(r.spent) + " / " + CurrencyUtil.format(r.budget);
            String pctStr = String.format("%.0f%%", r.percentUsed);

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            JLabel left = new JLabel(name + "  " + amountStr);
            left.setFont(left.getFont().deriveFont(Font.BOLD, 12f));
            row.add(left, BorderLayout.WEST);
            JLabel right = new JLabel(pctStr);
            right.setFont(right.getFont().deriveFont(Font.BOLD, 12f));
            right.setForeground(PERCENT_RED);
            right.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(right, BorderLayout.EAST);
            listPanel.add(row);
        }
        listPanel.revalidate();
        listPanel.repaint();
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
