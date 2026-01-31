package com.expensemanager.view.CommonComponents;

import com.expensemanager.model.WalletType;

import javax.swing.*;
import java.awt.*;

public class WalletListCellRenderer extends DefaultListCellRenderer {

  private final int iconSize;

  public WalletListCellRenderer() {
    this(24);
  }

  public WalletListCellRenderer(int iconSize) {
    this.iconSize = iconSize;
  }

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value instanceof WalletType) {
      WalletType wallet = (WalletType) value;
      label.setText(wallet.getDisplayName());
      if (wallet.getIconPath() != null) {
        ImageIcon icon = new ImageIcon(wallet.getIconPath());
        if (icon.getIconWidth() > 0) {
          Image img = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
          label.setIcon(new ImageIcon(img));
        }
      } else {
        label.setIcon(null);
      }
    } else if (value instanceof String) {
      label.setText((String) value);
    }

    label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    return label;
  }
}
