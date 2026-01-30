package com.expensemanager.view.CommonComponents;

import com.expensemanager.util.UIUtils;
import com.expensemanager.util.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SidebarButton extends JToggleButton {

    private final String cardName;
    private boolean hover;
    private ImageIcon originalIcon;
    private ImageIcon whiteIcon;
    private ImageIcon grayIcon;

    public SidebarButton(String cardName, String label, ImageIcon icon) {
        super(label);
        this.cardName = cardName;
        this.originalIcon = icon;

        // Create filtered icons for selected (white) and unselected (dark gray) states
        if (icon != null && icon.getImage() != null) {
            whiteIcon = createColoredIcon(icon, Color.WHITE);
            grayIcon = createColoredIcon(icon, new Color(UIFactory.COLOR_SIDEBAR_TEXT));
        }

        setOpaque(true);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        // Padding: top, left, bottom, right - left padding 20px as per spec
        setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Fixed height: 48px - let layout control width
        setPreferredSize(new Dimension(0, 48));
        setMinimumSize(new Dimension(0, 48));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // Font: 24px Regular/Medium
        setFont(getFont().deriveFont(Font.PLAIN, 20f));

        // Set initial icon
        updateIcon();

        getModel().addChangeListener(e -> {
            updateForeground();
            updateIcon();
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

    /**
     * Creates a colored version of an icon by applying a color tint.
     * Converts icon to grayscale and then applies target color.
     */
    private ImageIcon createColoredIcon(ImageIcon original, Color color) {
        if (original == null || original.getImage() == null) {
            return null;
        }

        Image image = original.getImage();
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        if (width <= 0 || height <= 0) {
            return original;
        }

        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffered.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Draw original image
        g2d.drawImage(image, 0, 0, null);

        // Convert to grayscale and apply color tint
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = buffered.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;

                if (alpha == 0) {
                    continue; // Keep transparent pixels
                }

                // Convert to grayscale
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                // Apply target color based on grayscale intensity
                float intensity = gray / 255.0f;
                int newR = (int) (color.getRed() * intensity);
                int newG = (int) (color.getGreen() * intensity);
                int newB = (int) (color.getBlue() * intensity);

                int newRgb = (alpha << 24) | (newR << 16) | (newG << 8) | newB;
                buffered.setRGB(x, y, newRgb);
            }
        }

        g2d.dispose();

        return new ImageIcon(buffered);
    }

    private void updateIcon() {
        if (originalIcon == null) {
            setIcon(null);
            return;
        }

        if (isSelected()) {
            setIcon(whiteIcon != null ? whiteIcon : originalIcon);
        } else {
            setIcon(grayIcon != null ? grayIcon : originalIcon);
        }
    }

    public String getCardName() {
        return cardName;
    }

    @Override
    public void setSelected(boolean selected) {
        boolean old = isSelected();
        super.setSelected(selected);
        if (old != selected) {
            updateForeground();
            updateIcon();
            repaint();
        }
    }

    /** Call when theme is toggled to refresh colors. */
    public void refreshTheme() {
        updateForeground();
        // Recreate colored icons if needed
        if (originalIcon != null) {
            grayIcon = createColoredIcon(originalIcon, new Color(UIFactory.COLOR_SIDEBAR_TEXT));
        }
        updateIcon();
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
            bg = UIUtils.getSidebarBackground(false);
        }
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), UIFactory.SIDEBAR_ARC, UIFactory.SIDEBAR_ARC);
        g2.dispose();

        super.paintComponent(g);
    }
}
