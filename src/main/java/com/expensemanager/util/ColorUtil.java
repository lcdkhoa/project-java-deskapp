package com.expensemanager.util;

import java.awt.Color;

public final class ColorUtil {

  public static final Color DEFAULT_GRAY = new Color(0x6B7280);
  public static final Color SECONDARY_GRAY = new Color(0x9CA3AF);

  private ColorUtil() {
  }

  public static Color parseColor(String hex) {
    return parseColor(hex, DEFAULT_GRAY);
  }

  public static Color parseColor(String hex, Color defaultColor) {
    if (hex == null || hex.isBlank()) {
      return defaultColor;
    }
    if (!hex.startsWith("#")) {
      hex = "#" + hex;
    }
    try {
      return Color.decode(hex);
    } catch (Exception e) {
      return defaultColor;
    }
  }
}
