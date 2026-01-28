package com.expensemanager.view;

import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.UIUtils;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Single budget record row in Budget page.
 * Fixed height, rounded card, custom progress bar & status chip.
 */
public class BudgetRowItem extends JPanel {

    private static final int CARD_ARC = 30;
    private static final int ROW_HEIGHT = 125;

    public BudgetRowItem(Category category,
            BudgetDAO.BudgetUsedRow usedRow,
            Budget budget,
            BudgetController controller) {
        super(new MigLayout("fill, insets 15 25 15 25", "[44!]20[grow]20[right]", "[center][center][bottom]"));
        // Store for edit action
        final Budget rowBudget = budget;
        final Category rowCategory = category;
        final BudgetController rowController = controller;

        setOpaque(false);
        setPreferredSize(new Dimension(0, ROW_HEIGHT));
        setMinimumSize(new Dimension(0, ROW_HEIGHT));

        String name = category != null ? category.getName() : usedRow.categoryId;

        // Left: category icon only (no colored circle per design)
        String iconPath = resolveIconPath(category);
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(44, 44));
        iconPanel.setMinimumSize(new Dimension(44, 44));

        JLabel iconLabel = new JLabel();
        ImageIcon icon = iconPath != null ? UIUtils.getIcon(iconPath, 24, 24) : null;
        if (icon != null) {
            iconLabel.setIcon(icon);
        }
        iconPanel.add(iconLabel);

        add(iconPanel, "cell 0 0, aligny center");

        // Row 0: category icon + category label on same line
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 16f));
        nameLabel.setForeground(new Color(0x111827));
        add(nameLabel, "cell 1 0, aligny center, growx");

        // Row 1: progress bar full width, fixed height 15px, arc 30px
        BudgetProgressBar bar = new BudgetProgressBar(15, 30);
        bar.setPercent(usedRow.percentUsed);
        add(bar, "cell 0 1 3 1, growx, h 15!");

        // Row 2: "Spent: X / Limit: Y" (left) + "%" (right) on same line
        String spentText = CurrencyUtil.format(usedRow.spent);
        String limitText = CurrencyUtil.format(usedRow.budget);
        JPanel infoRow = new JPanel(new MigLayout("ins 0, fillx", "[pref!][grow][pref!]", "[center]"));
        infoRow.setOpaque(false);
        
        JLabel infoLabel = new JLabel("Spent: " + spentText + " / Limit: " + limitText);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 14f));
        infoLabel.setForeground(new Color(0x6B7280));
        infoRow.add(infoLabel);
        
        // Empty space in middle
        infoRow.add(new JLabel(), "growx");
        
        // Percentage label (right aligned)
        JLabel percentLabel = new JLabel(String.format("%.0f%%", usedRow.percentUsed));
        percentLabel.setFont(percentLabel.getFont().deriveFont(Font.PLAIN, 14f));
        // Color based on percent: green < 80%, yellow 80-99%, red >= 100%
        if (usedRow.percentUsed < 80) {
            percentLabel.setForeground(new Color(0x22C55E)); // Green
        } else if (usedRow.percentUsed < 100) {
            percentLabel.setForeground(new Color(0xF59E0B)); // Yellow/Orange
        } else {
            percentLabel.setForeground(new Color(0xB91C1C)); // Red
        }
        infoRow.add(percentLabel);
        
        add(infoRow, "cell 1 2 2 1, aligny bottom, growx");

        // Right: status chip + edit button
        JPanel rightPanel = new JPanel(new MigLayout("ins 0, gap 8", "[][pref!]", "[center]"));
        rightPanel.setOpaque(false);

        Status status = Status.fromPercent(usedRow.percentUsed);
        StatusChip chip = new StatusChip(status);
        rightPanel.add(chip, "aligny center");

        JButton editButton = createEditButton();
        rightPanel.add(editButton, "aligny center");

        add(rightPanel, "cell 2 0, alignx right, aligny center");

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
        String dbIcon = category.getIcon();
        if (dbIcon != null && dbIcon.toLowerCase().endsWith(".png")) {
            return dbIcon;
        }
        String name = category.getName();
        if (name == null) {
            return null;
        }
        // Fallback mapping similar to TransactionView
        switch (name) {
            case "Food":
                return "imgs/category/food.png";
            case "Transport":
                return "imgs/category/transport.png";
            case "Shopping":
                return "imgs/category/shopping.png";
            case "Entertainment":
                return "imgs/category/entertainment.png";
            case "Bills":
                return "imgs/category/bill.png";
            case "Healthcare":
                return "imgs/category/healthcare.png";
            case "Housing":
                return "imgs/category/housing.png";
            case "Education":
                return "imgs/category/education.png";
            case "Other":
                return "imgs/category/others.png";
            case "Salary":
                return "imgs/category/salary.png";
            case "Freelance":
                return "imgs/category/freelance.png";
            case "Affiliate":
                return "imgs/category/affiliate.png";
            case "Selling":
                return "imgs/category/selling.png";
            case "Other Income":
                return "imgs/category/other_income.png";
            default:
                return "imgs/category/others.png";
        }
    }

    private JButton createEditButton() {
        JButton btn = new JButton();
        ImageIcon penIcon = UIUtils.getIcon("imgs/budget/pen.png", 20, 20);
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
