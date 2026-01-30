package com.expensemanager.util;

import java.awt.Color;

/**
 * Utility class for color parsing and manipulation.
 * Provides centralized color handling to avoid code duplication.
 */
public final class ColorUtil {

  /** Default gray color used when parsing fails */
  public static final Color DEFAULT_GRAY = new Color(0x6B7280);

  /** Secondary default gray color */
  public static final Color SECONDARY_GRAY = new Color(0x9CA3AF);

  private ColorUtil() {
    // Utility class, prevent instantiation
  }

  /**
   * Parse a hex color string to Color object.
   * Handles both "#RRGGBB" and "RRGGBB" formats.
   * 
   * @param hex the hex color string
   * @return the parsed Color, or DEFAULT_GRAY if parsing fails
   */
  public static Color parseColor(String hex) {
    return parseColor(hex, DEFAULT_GRAY);
  }

  /**
   * Parse a hex color string to Color object with custom default.
   * Handles both "#RRGGBB" and "RRGGBB" formats.
   * 
   * @param hex          the hex color string
   * @param defaultColor the color to return if parsing fails
   * @return the parsed Color, or defaultColor if parsing fails
   */
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
