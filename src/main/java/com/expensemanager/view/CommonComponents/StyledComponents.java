package com.expensemanager.view.CommonComponents;

import javax.swing.*;
import java.awt.*;

/**
 * Common styled UI components factory to avoid code duplication.
 * Provides styled text fields, combo boxes, and buttons used across views.
 */
public final class StyledComponents {

    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final Color FOCUS_BORDER_COLOR = new Color(0x155DFC);
    private static final int DEFAULT_ARC = 30;
    private static final int DEFAULT_CONTROL_HEIGHT = 48;

    private StyledComponents() {
    }

    /**
     * Creates a styled text field with rounded corners, white background, and
     * border.
     * Default: arc 30, height 48px, padding 16px left/right.
     */
    public static JTextField createStyledTextField() {
        return createStyledTextField(0, DEFAULT_CONTROL_HEIGHT, DEFAULT_ARC);
    }

    /**
     * Creates a styled text field with custom dimensions.
     *
     * @param width  preferred width (0 = flexible)
     * @param height fixed height
     * @param arc    corner radius
     */
    public static JTextField createStyledTextField(int width, int height, int arc) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 14f));
        if (width > 0) {
            field.setPreferredSize(new Dimension(width, height));
            field.setMinimumSize(new Dimension(width, height));
            field.setMaximumSize(new Dimension(width, height));
        } else {
            field.setPreferredSize(new Dimension(0, height));
            field.setMinimumSize(new Dimension(0, height));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        }
        return field;
    }

    /**
     * Creates a styled combo box with rounded corners, white background, custom
     * arrow button,
     * and blue border on focus/popup. Default: arc 30, height 48px.
     */
    public static <T> JComboBox<T> createStyledComboBox() {
        return createStyledComboBox(DEFAULT_CONTROL_HEIGHT, DEFAULT_ARC);
    }

    /**
     * Creates a styled combo box with custom height and arc.
     */
    public static <T> JComboBox<T> createStyledComboBox(int height, int arc) {
        JComboBox<T> combo = new JComboBox<T>() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
                super.paintComponent(g);
                Graphics2D g2Border = (Graphics2D) g.create();
                g2Border.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color borderColor = (isFocusOwner() || isPopupVisible()) ? FOCUS_BORDER_COLOR : BORDER_COLOR;
                g2Border.setColor(borderColor);
                g2Border.setStroke(new BasicStroke(1));
                g2Border.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2Border.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Border is painted inside paintComponent.
            }
        };
        combo.setOpaque(false);
        combo.setPreferredSize(new Dimension(0, height));
        combo.setMinimumSize(new Dimension(0, height));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        combo.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        combo.setFont(combo.getFont().deriveFont(Font.PLAIN, 14f));

        try {
            combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton button = new JButton() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(new Color(0x6B7280));

                            int width = getWidth();
                            int height = getHeight();
                            int arrowSize = 12;
                            int x = (width - arrowSize) / 2;
                            int y = (height - arrowSize) / 2;

                            int[] xPoints = { x + arrowSize / 2, x, x + arrowSize };
                            int[] yPoints = { y + arrowSize, y + 2, y + 2 };
                            g2.fillPolygon(xPoints, yPoints, 3);

                            g2.dispose();
                        }
                    };
                    button.setOpaque(false);
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                    button.setFocusPainted(false);
                    button.setPreferredSize(new Dimension(40, height));
                    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    return button;
                }
            });
        } catch (Exception e) {
            // Fallback: rely on default UI if custom UI is not available.
        }

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(FOCUS_BORDER_COLOR);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // Repaint when popup opens/closes to update border color
        combo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                combo.repaint();
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                combo.repaint();
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                combo.repaint();
            }
        });

        combo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                combo.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                combo.repaint();
            }
        });

        return combo;
    }

    /**
     * Creates a footer button (Cancel/Save style) with rounded corners.
     * Primary button: blue background (#2563EB or custom), white text, bold.
     * Secondary button: white background, gray border, black text.
     *
     * @param text         button label
     * @param primary      true for primary (blue), false for secondary (white with
     *                     border)
     * @param primaryColor custom primary color (if null, uses default #2563EB)
     */
    public static JButton createFooterButton(String text, boolean primary, Color primaryColor) {
        Color bgColor = primary ? (primaryColor != null ? primaryColor : new Color(0x2563EB)) : Color.WHITE;
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill white background first to avoid gray showing through rounded corners
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DEFAULT_ARC, DEFAULT_ARC);

                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DEFAULT_ARC, DEFAULT_ARC);

                if (!primary) {
                    g2.setColor(BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DEFAULT_ARC, DEFAULT_ARC);
                }

                g2.setColor(primary ? Color.WHITE : Color.BLACK);
                Font font = getFont().deriveFont(primary ? Font.BOLD : Font.PLAIN, 14f);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics(font);
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }

    /**
     * Creates a footer button with default primary color (#2563EB).
     */
    public static JButton createFooterButton(String text, boolean primary) {
        return createFooterButton(text, primary, null);
    }
}
