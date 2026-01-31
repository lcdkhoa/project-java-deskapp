package com.expensemanager.view.CommonComponents;

import javax.swing.*;
import java.awt.*;

public class SortListCellRenderer extends DefaultListCellRenderer {

  private final int iconSize;

  public SortListCellRenderer() {
    this(24);
  }

  public SortListCellRenderer(int iconSize) {
    this.iconSize = iconSize;
  }

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value instanceof SortItem) {
      SortItem item = (SortItem) value;
      label.setText(item.getLabel());
      if (item.getIconPath() != null) {
        ImageIcon icon = new ImageIcon(item.getIconPath());
        if (icon.getIconWidth() > 0) {
          Image img = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
          label.setIcon(new ImageIcon(img));
        }
      } else {
        label.setIcon(null);
      }
    }

    label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    return label;
  }

  public static class SortItem {
    private final String key;
    private final String label;
    private final String iconPath;

    public SortItem(String key, String label, String iconPath) {
      this.key = key;
      this.label = label;
      this.iconPath = iconPath;
    }

    public String getKey() {
      return key;
    }

    public String getLabel() {
      return label;
    }

    public String getIconPath() {
      return iconPath;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
