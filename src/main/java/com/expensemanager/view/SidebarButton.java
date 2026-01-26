package com.expensemanager.view;

import com.expensemanager.FlatLaf;
import com.expensemanager.util.UIUtils;
import com.expensemanager.util.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Sidebar navigation button: rounded rect (~12px), one selected at a time.
 * Selected: #2563EB bg, white text. Unselected: transparent bg, #374151.
 * Hover (unselected): #F3F4F6. Left-aligned text and icon.
 */
public class SidebarButton extends JToggleButton {

    private final String cardName;
    private boolean hover;

    public SidebarButton(String cardName, String label) {
        super(label);
        this.cardName = cardName;
        setOpaque(true);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        // Arc: we paint our own rounded rect in paintComponent; FlatLaf.style does not support "arc"

        getModel().addChangeListener(e -> {
            updateForeground();
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });

        updateForeground();
    }

    public String getCardName() {
        return cardName;
    }

    /** Call when theme is toggled to refresh colors. */
    public void refreshTheme() {
        updateForeground();
        repaint();
    }

    private void updateForeground() {
        setForeground(isSelected() ? Color.WHITE : UIFactory.getSidebarTextColor());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg;
        if (isSelected()) {
            bg = UIFactory.getPrimaryBlue();
        } else if (hover) {
            bg = UIFactory.getSidebarHoverColor();
        } else {
            bg = UIUtils.getSidebarBackground(FlatLaf.isDark());
        }
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIFactory.SIDEBAR_ARC, UIFactory.SIDEBAR_ARC);
        g2.dispose();

        super.paintComponent(g);
    }
}
