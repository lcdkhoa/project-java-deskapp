package com.expensemanager.view.CommonComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public final class StyledComponents {
    public static final int COLOR_PRIMARY = 0x4F46E5;
    public static final int COLOR_MAIN_BG_LIGHT = 0xFFFFFF;
    public static final int COLOR_CARD_BG_LIGHT = 0xFFFFFF;
    public static final int COLOR_SIDEBAR_BG_LIGHT = 0xFFFFFF;
    public static final int COLOR_SIDEBAR_BORDER_LIGHT = 0xE5E7EB;
    public static final int COLOR_PRIMARY_BLUE = 0x2563EB;
    public static final int COLOR_SIDEBAR_TEXT = 0x374151;
    public static final int COLOR_SIDEBAR_HOVER = 0xF3F4F6;
    public static final int COLOR_FOCUS_BORDER = 0x155DFC;

    public static final int ARC = 30;
    public static final Insets BUTTON_MARGIN = new Insets(8, 16, 8, 16);

    private static final Color BORDER_COLOR = new Color(COLOR_SIDEBAR_BORDER_LIGHT);
    private static final Color FOCUS_BORDER_COLOR = new Color(COLOR_FOCUS_BORDER);
    private static final Color PRIMARY_BLUE = new Color(COLOR_PRIMARY_BLUE);
    private static final Color TOGGLE_INACTIVE_BG = new Color(COLOR_SIDEBAR_HOVER);
    private static final int DEFAULT_CONTROL_HEIGHT = 48;

    public static final int BUTTON_HEIGHT_TITLE = 40;
    public static final int BUTTON_HEIGHT_FUNCTION = 48;

    private StyledComponents() {
    }

    public static JTextField createStyledTextField() {
        return createStyledTextField(0, DEFAULT_CONTROL_HEIGHT, ARC);
    }

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

    public static <T> JComboBox<T> createStyledComboBox() {
        return createStyledComboBox(DEFAULT_CONTROL_HEIGHT, ARC);
    }

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

    public enum ButtonType {
        PRIMARY,
        SECONDARY,
        DANGER,
        SUCCESS
    }

    public enum ButtonSize {
        TITLE(BUTTON_HEIGHT_TITLE),
        FUNCTION(BUTTON_HEIGHT_FUNCTION);

        private final int height;

        ButtonSize(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }
    }

    public static JButton createStyledButton(String text, ButtonType type, ButtonSize size, int width) {
        return createStyledButton(text, null, type, size, width);
    }

    private static Color[] getColorsForButtonType(ButtonType type) {
        switch (type) {
            case PRIMARY:
                return new Color[] { PRIMARY_BLUE, Color.WHITE };
            case SECONDARY:
                return new Color[] { Color.WHITE, Color.BLACK };
            case DANGER:
                return new Color[] { new Color(0xE7000B), Color.WHITE };
            case SUCCESS:
                return new Color[] { new Color(0x00A63E), Color.WHITE };
            default:
                return new Color[] { PRIMARY_BLUE, Color.WHITE };
        }
    }

    public static JButton createStyledButton(String text, Icon icon, ButtonType type, ButtonSize size, int width) {
        Color[] colors = getColorsForButtonType(type);
        final Color bgColor = colors[0];
        final Color fgColor = colors[1];
        final Color borderColor = type == ButtonType.SECONDARY ? BORDER_COLOR : null;
        final boolean hasBorder = type == ButtonType.SECONDARY;

        int btnHeight = size.getHeight();

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

                if (hasBorder && borderColor != null) {
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
                }

                Font font = getFont().deriveFont(type == ButtonType.SECONDARY ? Font.PLAIN : Font.BOLD, 14f);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics(font);

                Icon btnIcon = getIcon();
                int iconWidth = btnIcon != null ? btnIcon.getIconWidth() : 0;
                int iconTextGap = btnIcon != null ? getIconTextGap() : 0;
                int textWidth = fm.stringWidth(getText());
                int totalWidth = iconWidth + iconTextGap + textWidth;

                int startX = (getWidth() - totalWidth) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                if (btnIcon != null) {
                    int iconY = (getHeight() - btnIcon.getIconHeight()) / 2;
                    btnIcon.paintIcon(this, g2, startX, iconY);
                    startX += iconWidth + iconTextGap;
                }
                g2.setColor(fgColor);
                g2.drawString(getText(), startX, textY);

                g2.dispose();
            }
        };

        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(8);
        }

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (width > 0) {
            btn.setPreferredSize(new Dimension(width, btnHeight));
            btn.setMinimumSize(new Dimension(width, btnHeight));
            btn.setMaximumSize(new Dimension(width, btnHeight));
        } else {
            btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 32, btnHeight));
            btn.setMinimumSize(new Dimension(0, btnHeight));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnHeight));
        }

        return btn;
    }

    public static JToggleButton createStyledToggleButton(String text, ButtonType typeWhenSelected, ButtonSize size,
            int width) {
        Color[] selectedColors = getColorsForButtonType(typeWhenSelected);
        final Color selectedBg = selectedColors[0];
        final Color selectedFg = selectedColors[1];

        int btnHeight = size.getHeight();
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ARC, ARC));
                Color bg = isSelected() ? selectedBg : TOGGLE_INACTIVE_BG;
                Color fg = isSelected() ? selectedFg : Color.BLACK;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 14f));
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics(g2.getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(width, btnHeight));
        btn.setMinimumSize(new Dimension(width, btnHeight));
        btn.setMaximumSize(new Dimension(width, btnHeight));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 14f));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createTitleButton(String text, Icon icon, int width) {
        return createStyledButton(text, icon, ButtonType.PRIMARY, ButtonSize.TITLE, width);
    }

    public static JButton createPrimaryFunctionButton(String text, int width) {
        return createStyledButton(text, null, ButtonType.PRIMARY, ButtonSize.FUNCTION, width);
    }

    public static JButton createSecondaryFunctionButton(String text, int width) {
        return createStyledButton(text, null, ButtonType.SECONDARY, ButtonSize.FUNCTION, width);
    }

    public static Icon createPlusIcon() {
        int s = 16;
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        int t = 2;
        int m = s / 2;
        g.fillRect(m - t / 2, 2, t, s - 4);
        g.fillRect(2, m - t / 2, s - 4, t);
        g.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon getIcon(String path, int width, int height) {
        try {
            if (path == null || path.isBlank())
                return null;
            String resourceName = toClasspathResource(path);
            if (resourceName != null) {
                try (InputStream in = StyledComponents.class.getClassLoader().getResourceAsStream(resourceName)) {
                    if (in != null) {
                        BufferedImage img = ImageIO.read(in);
                        if (img != null) {
                            java.awt.Image scaled = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
                            return new ImageIcon(scaled);
                        }
                    }
                }
            }
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                String normalized = path.replace("\\", "/");
                if (normalized.startsWith("imgs/")) {
                    filePath = Paths.get("src/main/java/com/expensemanager/img",
                            normalized.substring("imgs/".length()));
                } else {
                    filePath = Paths.get("src/main/java/com/expensemanager/img", normalized);
                }
            }
            if (Files.exists(filePath)) {
                java.awt.Image img = ImageIO.read(filePath.toFile());
                if (img != null) {
                    img = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static String toClasspathResource(String path) {
        if (path == null)
            return null;
        String n = path.replace("\\", "/");
        if (n.contains("com/expensemanager/img/")) {
            int i = n.indexOf("com/expensemanager/img/");
            return n.substring(i);
        }
        if (n.startsWith("src/main/java/"))
            return n.substring("src/main/java/".length());
        if (n.startsWith("imgs/"))
            return "com/expensemanager/img/" + n.substring("imgs/".length());
        return null;
    }
}
