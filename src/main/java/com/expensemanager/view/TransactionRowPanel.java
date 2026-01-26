package com.expensemanager.view;

import com.expensemanager.util.CurrencyUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Card-like row for Section 2.2: Category icon (circle), Note (bold) + Wallet (gray),
 * Amount (red/green) + Time. Padding for spacing.
 */
public class TransactionRowPanel extends JPanel {

    private static final Color NOTE_COLOR = new Color(0x111827);
    private static final Color WALLET_COLOR = new Color(0x6B7280);
    private static final Color EXPENSE_COLOR = new Color(0xB91C1C);
    private static final Color INCOME_COLOR = new Color(0x16A34A);
    private static final int ICON_SIZE = 40;
    private static final int ROW_PADDING = 10;

    public TransactionRowPanel(String icon, Color iconBgColor, String note, String wallet,
                              long amount, String timeHhmm) {
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(ROW_PADDING, 12, ROW_PADDING, 12));
        setOpaque(true);
        setBackground(Color.WHITE);

        // Left: circular icon
        JPanel iconWrap = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBgColor != null ? iconBgColor : WALLET_COLOR);
                int s = Math.min(getWidth(), getHeight());
                g2.fillOval((getWidth() - s) / 2, (getHeight() - s) / 2, s, s);
                g2.dispose();
            }
        };
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        JLabel iconLbl = new JLabel(icon != null && !icon.isEmpty() ? icon : "•");
        iconLbl.setFont(iconLbl.getFont().deriveFont(18f));
        iconLbl.setForeground(Color.WHITE);
        iconWrap.add(iconLbl);
        add(iconWrap, BorderLayout.WEST);

        // Middle: Note (bold), Wallet (small gray)
        JPanel mid = new JPanel(new BorderLayout(0, 2));
        mid.setOpaque(false);
        JLabel noteLbl = new JLabel(note != null && !note.isBlank() ? note : "(No note)");
        noteLbl.setFont(noteLbl.getFont().deriveFont(Font.BOLD, 13f));
        noteLbl.setForeground(NOTE_COLOR);
        mid.add(noteLbl, BorderLayout.NORTH);
        JLabel walletLbl = new JLabel(wallet != null ? wallet : "");
        walletLbl.setFont(walletLbl.getFont().deriveFont(11f));
        walletLbl.setForeground(WALLET_COLOR);
        mid.add(walletLbl, BorderLayout.CENTER);
        add(mid, BorderLayout.CENTER);

        // Right: Amount (red/green), Time (HH:mm)
        JPanel right = new JPanel(new BorderLayout(0, 2));
        right.setOpaque(false);
        JLabel amountLbl = new JLabel(CurrencyUtil.formatSigned(amount));
        amountLbl.setFont(amountLbl.getFont().deriveFont(Font.BOLD, 13f));
        amountLbl.setForeground(amount < 0 ? EXPENSE_COLOR : INCOME_COLOR);
        amountLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        right.add(amountLbl, BorderLayout.NORTH);
        JLabel timeLbl = new JLabel(timeHhmm != null ? timeHhmm : "");
        timeLbl.setFont(timeLbl.getFont().deriveFont(11f));
        timeLbl.setForeground(WALLET_COLOR);
        timeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        right.add(timeLbl, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }
}
