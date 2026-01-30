package com.expensemanager.view.TransactionView;

import com.expensemanager.view.CommonComponents.StyledComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CategoryItemPanel extends JPanel {
    private static final int PANEL_WIDTH = 140;
    private static final int PANEL_HEIGHT = 75;
    private static final int ARC = 30;
    private static final int ICON_SIZE = 30;
    private static final Color SELECTED_BORDER_COLOR = new Color(0x155DFC);
    private static final Color DEFAULT_BORDER_COLOR = new Color(0xE5E7EB);
    private static final int SELECTED_BORDER_WIDTH = 2;
    private static final int DEFAULT_BORDER_WIDTH = 1;

    private final String categoryId;
    private final String categoryName;
    private final String iconPath;
    private boolean selected;
    private JLabel iconLabel;
    private JLabel nameLabel;

    public CategoryItemPanel(String categoryId, String categoryName, String iconPath) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.iconPath = iconPath;
        this.selected = false;

        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setMaximumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setOpaque(false);
        setLayout(new BorderLayout(0, 8));

        iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadIcon();
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setOpaque(false);
        iconPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        iconPanel.add(iconLabel, BorderLayout.CENTER);
        add(iconPanel, BorderLayout.CENTER);

        nameLabel = new JLabel(categoryName);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 14f));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(nameLabel, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(true);
            }
        });

        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadIcon() {
        ImageIcon icon = StyledComponents.getIcon(iconPath, ICON_SIZE, ICON_SIZE);
        if (icon != null) {
            iconLabel.setIcon(icon);
        } else {
            iconLabel.setText("?");
            iconLabel.setFont(iconLabel.getFont().deriveFont(Font.BOLD, 16f));
        }
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            repaint();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

        if (selected) {
            g2.setStroke(new BasicStroke(SELECTED_BORDER_WIDTH));
            g2.setColor(SELECTED_BORDER_COLOR);
        } else {
            g2.setStroke(new BasicStroke(DEFAULT_BORDER_WIDTH));
            g2.setColor(DEFAULT_BORDER_COLOR);
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

        g2.dispose();
        super.paintComponent(g);
    }
}
