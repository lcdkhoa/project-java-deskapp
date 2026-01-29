package com.expensemanager.view.TransactionView;

import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Card-like row for Section 2.2: Category icon (circle), Note (bold) + Wallet
 * (gray),
 * Amount (red/green) + Time. Padding for spacing.
 * iconPath: path to category image (e.g.
 * src/main/java/com/expensemanager/img/category/food.png); rendered as image
 * when path-like.
 */
public class TransactionRowPanel extends JPanel {

    private static final Color NOTE_COLOR = new Color(0x111827);
    private static final Color WALLET_COLOR = new Color(0x6B7280);
    private static final Color EXPENSE_COLOR = new Color(0xB91C1C);
    private static final Color INCOME_COLOR = new Color(0x16A34A);
    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final int ICON_SIZE = 48;
    private static final int ROW_PADDING = 14;
    private static final int CARD_ARC = 30;

    // Chip colors
    private static final Color CATEGORY_CHIP_BG = new Color(0xEDE9FE); // Light purple
    private static final Color CATEGORY_CHIP_FG = new Color(0x7C3AED); // Purple text
    private static final Color WALLET_CHIP_BG = new Color(0xE0F2FE); // Light blue
    private static final Color WALLET_CHIP_FG = new Color(0x0369A1); // Blue text

    public static final String DEFAULT_CATEGORY_ICON = "src/main/java/com/expensemanager/img/category/others.png";

    public TransactionRowPanel(String iconPath, Color iconBgColor, String note, String wallet,
            long amount, String timeHhmm) {
        this(iconPath, iconBgColor, note, null, wallet, amount, timeHhmm);
    }

    @SuppressWarnings("unused")
    public TransactionRowPanel(String iconPath, Color iconBgColor, String note, String categoryName, String wallet,
            long amount, String timeHhmm) {
        // iconBgColor is kept for backward compatibility but no longer used
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(ROW_PADDING, 16, ROW_PADDING, 16));
        setOpaque(false);

        // Left: category icon only (no circle background), 40x40
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        JLabel iconLbl = new JLabel();
        if (iconPath != null && !iconPath.isBlank()
                && (iconPath.contains("/") || iconPath.toLowerCase().endsWith(".png"))) {
            ImageIcon img = UIUtils.getIcon(iconPath, ICON_SIZE, ICON_SIZE);
            if (img != null) {
                iconLbl.setIcon(img);
            }
        }
        iconWrap.add(iconLbl);
        add(iconWrap, BorderLayout.WEST);

        // Middle: Note (bold), chips row (category chip + wallet chip)
        JPanel mid = new JPanel(new BorderLayout(0, 6));
        mid.setOpaque(false);
        JLabel noteLbl = new JLabel(note != null && !note.isBlank() ? note : "(No note)");
        noteLbl.setFont(noteLbl.getFont().deriveFont(Font.BOLD, 14f));
        noteLbl.setForeground(NOTE_COLOR);
        mid.add(noteLbl, BorderLayout.NORTH);

        // Chips row: category chip + wallet chip
        JPanel chipsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipsRow.setOpaque(false);

        // Category chip (if provided)
        if (categoryName != null && !categoryName.isBlank()) {
            JComponent categoryChip = createChip(categoryName, CATEGORY_CHIP_BG, CATEGORY_CHIP_FG);
            chipsRow.add(categoryChip);
        }

        // Wallet chip
        if (wallet != null && !wallet.isBlank()) {
            JComponent walletChip = createChip(wallet, WALLET_CHIP_BG, WALLET_CHIP_FG);
            chipsRow.add(walletChip);
        }

        mid.add(chipsRow, BorderLayout.CENTER);
        add(mid, BorderLayout.CENTER);

        // Right: Amount (red/green), Time (HH:mm)
        JPanel right = new JPanel(new BorderLayout(0, 4));
        right.setOpaque(false);
        JLabel amountLbl = new JLabel(CurrencyUtil.formatSigned(amount));
        amountLbl.setFont(amountLbl.getFont().deriveFont(Font.BOLD, 14f));
        amountLbl.setForeground(amount < 0 ? EXPENSE_COLOR : INCOME_COLOR);
        amountLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        right.add(amountLbl, BorderLayout.NORTH);
        JLabel timeLbl = new JLabel(timeHhmm != null ? timeHhmm : "");
        timeLbl.setFont(timeLbl.getFont().deriveFont(12f));
        timeLbl.setForeground(WALLET_COLOR);
        timeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        right.add(timeLbl, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    /**
     * Create a styled chip component with rounded background.
     */
    private JComponent createChip(String text, Color bgColor, Color fgColor) {
        final int CHIP_HEIGHT = 22;
        final int CHIP_PADDING_H = 10;
        final int CHIP_ARC = 12;
        final Font chipFont = new Font("SansSerif", Font.PLAIN, 11);

        // Calculate width based on text
        JLabel temp = new JLabel(text);
        temp.setFont(chipFont);
        int textWidth = temp.getFontMetrics(chipFont).stringWidth(text);
        final int chipWidth = textWidth + CHIP_PADDING_H * 2;

        JComponent chip = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw background
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CHIP_ARC, CHIP_ARC);

                // Draw text
                g2.setColor(fgColor);
                g2.setFont(chipFont);
                FontMetrics fm = g2.getFontMetrics(chipFont);
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, x, y);

                g2.dispose();
            }
        };

        chip.setOpaque(false);
        chip.setPreferredSize(new Dimension(chipWidth, CHIP_HEIGHT));
        chip.setMinimumSize(new Dimension(chipWidth, CHIP_HEIGHT));
        chip.setMaximumSize(new Dimension(chipWidth, CHIP_HEIGHT));

        return chip;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw white rounded card background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC);

        // Draw border
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CARD_ARC, CARD_ARC);

        g2.dispose();
        super.paintComponent(g);
    }
}
