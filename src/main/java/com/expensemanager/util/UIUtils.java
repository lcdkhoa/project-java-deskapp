package com.expensemanager.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.imageio.ImageIO;

public final class UIUtils {

    public static final int COLOR_PRIMARY = 0x4F46E5;
    public static final int COLOR_MAIN_BG_LIGHT = 0xFFFFFF;
    public static final int COLOR_CARD_BG_LIGHT = 0xFFFFFF;
    public static final int COLOR_SIDEBAR_BG_LIGHT = 0xFFFFFF;
    public static final int COLOR_SIDEBAR_BORDER_LIGHT = 0xE5E7EB;
    private static final int ARC = 30;

    private UIUtils() {
    }

    public static Font getBaseFont() {
        String[] tryNames = { "Segoe UI", "Inter" };
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String name : tryNames) {
            if (Arrays.asList(available).contains(name)) {
                return new Font(name, Font.PLAIN, 14);
            }
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    }

    public static void applyTheme() {
        UIManager.put("Button.arc", 999);
        UIManager.put("ToggleButton.arc", ARC);
        UIManager.put("Component.arc", ARC);
        UIManager.put("TextComponent.arc", ARC);
        UIManager.put("CheckBox.arc", ARC);
        UIManager.put("ProgressBar.arc", ARC);

        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);

        Font base = getBaseFont();
        UIManager.put("Button.font", base);
        UIManager.put("Label.font", base);
        UIManager.put("TextField.font", base);
        UIManager.put("TextArea.font", base);
        UIManager.put("ComboBox.font", base);
        UIManager.put("Table.font", base);
        UIManager.put("TitledBorder.font", base);
        UIManager.put("List.font", base);
    }

    public static ImageIcon getIcon(String path, int width, int height) {
        try {
            if (path == null || path.isBlank()) {
                return null;
            }

            String resourceName = toClasspathResource(path);
            if (resourceName != null) {
                try (InputStream in = UIUtils.class.getClassLoader().getResourceAsStream(resourceName)) {
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
            // File not found or read error - return null
        }
        return null;
    }

    /**
     * Convert path to classpath resource name (e.g. com/expensemanager/img/...).
     */
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
