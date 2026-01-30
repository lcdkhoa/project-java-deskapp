package com.expensemanager.view.BudgetView;

import com.expensemanager.controller.BudgetController;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.view.CommonComponents.StyledComponents;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class BudgetRowItem extends JPanel {

    private static final int CARD_ARC = 30;
    private static final int ROW_HEIGHT = 125;

    private static final int ICON_SIZE = 48;

    public BudgetRowItem(Category category,
            BudgetDAO.BudgetUsedRow usedRow,
            Budget budget,
            BudgetController controller) {
        super(new MigLayout("fill, insets 15 25 15 25", "[" + ICON_SIZE + "!]20[grow]20[right]",
                "[center][center][bottom]"));
        final Budget rowBudget = budget;
        final Category rowCategory = category;
        final BudgetController rowController = controller;

        setOpaque(false);
        setPreferredSize(new Dimension(0, ROW_HEIGHT));
        setMinimumSize(new Dimension(0, ROW_HEIGHT));

        String name = category != null ? category.getName() : usedRow.categoryId;

        String iconPath = resolveIconPath(category);
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        iconPanel.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));

        JLabel iconLabel = new JLabel();
        ImageIcon icon = iconPath != null ? StyledComponents.getIcon(iconPath, ICON_SIZE, ICON_SIZE) : null;
        if (icon != null) {
            iconLabel.setIcon(icon);
        }
        iconPanel.add(iconLabel);

        add(iconPanel, "cell 0 0, aligny center");

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 16f));
        nameLabel.setForeground(new Color(0x111827));
        add(nameLabel, "cell 1 0, aligny center, growx");

        BudgetProgressBar bar = new BudgetProgressBar(15, 30);
        bar.setPercent(usedRow.percentUsed);
        add(bar, "cell 0 1 3 1, growx, h 15!");

        String spentText = CurrencyUtil.format(usedRow.spent);
        String limitText = CurrencyUtil.format(usedRow.budget);
        JPanel infoRow = new JPanel(new MigLayout("ins 0, fillx", "[pref!][grow][pref!]", "[center]"));
        infoRow.setOpaque(false);

        JLabel infoLabel = new JLabel("Spent: " + spentText + " / Limit: " + limitText);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 14f));
        infoLabel.setForeground(new Color(0x6B7280));
        infoRow.add(infoLabel);

        infoRow.add(new JLabel(), "growx");

        JLabel percentLabel = new JLabel(String.format("%.0f%%", usedRow.percentUsed));
        percentLabel.setFont(percentLabel.getFont().deriveFont(Font.PLAIN, 14f));
        if (usedRow.percentUsed < 80) {
            percentLabel.setForeground(new Color(0x22C55E));
        } else if (usedRow.percentUsed < 100) {
            percentLabel.setForeground(new Color(0xF59E0B));
        } else {
            percentLabel.setForeground(new Color(0xB91C1C));
        }
        infoRow.add(percentLabel);

        add(infoRow, "cell 0 2 3 1, aligny bottom, growx");

        JPanel rightPanel = new JPanel(new MigLayout("ins 0, gap 8", "[][pref!][pref!]", "[center]"));
        rightPanel.setOpaque(false);

        Status status = Status.fromPercent(usedRow.percentUsed);
        StatusChip chip = new StatusChip(status);
        rightPanel.add(chip, "aligny center");

        JButton deleteButton = createDeleteButton();
        rightPanel.add(deleteButton, "aligny center");

        JButton editButton = createEditButton();
        rightPanel.add(editButton, "aligny center");

        add(rightPanel, "cell 2 0, alignx right, aligny center");

        deleteButton.addActionListener(e -> {
            if (rowBudget != null && rowController != null) {
                rowController.deleteBudget(rowBudget);
            }
        });

        editButton.addActionListener(e -> {
            if (rowBudget != null && rowController != null) {
                rowController.openEditBudget(rowBudget, rowCategory);
            }
        });
    }

    private String resolveIconPath(Category category) {
        if (category == null) {
            return null;
        }
        String path = category.getIconPath();
        return path;

    }

    private JButton createDeleteButton() {
        JButton btn = new JButton();
        ImageIcon deleteIcon = StyledComponents.getIcon("src/main/java/com/expensemanager/img/budget/delete.png", 20,
                20);
        if (deleteIcon != null) {
            btn.setIcon(deleteIcon);
        }
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setMinimumSize(new Dimension(28, 28));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createEditButton() {
        JButton btn = new JButton();
        ImageIcon penIcon = StyledComponents.getIcon("src/main/java/com/expensemanager/img/budget/pen.png", 20, 20);
        if (penIcon != null) {
            btn.setIcon(penIcon);
        }
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setMinimumSize(new Dimension(28, 28));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private enum Status {
        SAFE, NEAR_LIMIT, EXCEEDED;

        static Status fromPercent(double percent) {
            if (percent < 80.0) {
                return SAFE;
            } else if (percent < 100.0) {
                return NEAR_LIMIT;
            } else {
                return EXCEEDED;
            }
        }
    }

    private static class StatusChip extends JComponent {

        private static final int CHIP_WIDTH = 90;
        private static final int CHIP_HEIGHT = 24;
        private final Status status;

        StatusChip(Status status) {
            this.status = status;
            setPreferredSize(new Dimension(CHIP_WIDTH, CHIP_HEIGHT));
            setMinimumSize(new Dimension(CHIP_WIDTH, CHIP_HEIGHT));
            setMaximumSize(new Dimension(CHIP_WIDTH, CHIP_HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg;
            Color fg;
            String text;
            switch (status) {
                case SAFE:
                    bg = new Color(0xDCFCE7);
                    fg = new Color(0x166534);
                    text = "Safe";
                    break;
                case NEAR_LIMIT:
                    bg = new Color(0xFEF3C7);
                    fg = new Color(0x92400E);
                    text = "Near Limit";
                    break;
                default:
                    bg = new Color(0xFEE2E2);
                    fg = new Color(0xB91C1C);
                    text = "Exceeded";
                    break;
            }

            int w = getWidth();
            int h = getHeight();
            int arc = 24;

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(fg);
            Font font = getFont().deriveFont(Font.BOLD, 11f);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics(font);
            int x = (w - fm.stringWidth(text)) / 2;
            int y = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
        g2.setColor(new Color(0xE5E7EB));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CARD_ARC, CARD_ARC);
        g2.dispose();
        super.paintComponent(g);
    }
}
