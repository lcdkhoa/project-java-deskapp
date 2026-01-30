package com.expensemanager.view.CommonComponents;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Reusable Time Picker component.
 * Displays a popup time selector with 30-minute intervals.
 */
public class TimePicker extends JPanel {

  private static final Color BORDER_COLOR = new Color(0xE5E7EB);
  private static final Color FOCUS_COLOR = new Color(0x155DFC);
  private static final Color SELECTED_BG = new Color(0x155DFC);
  private static final Color HOVER_BG = new Color(0xE3F2FD);
  private static final int ARC = 30;
  private static final int FIELD_HEIGHT = 48;

  private final JTextField timeField;
  private Date selectedTime;
  private Consumer<Date> onTimeSelected;

  private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

  public TimePicker() {
    this(null);
  }

  public TimePicker(String placeholder) {
    setLayout(new BorderLayout());
    setOpaque(false);
    setPreferredSize(new Dimension(0, FIELD_HEIGHT));
    setMinimumSize(new Dimension(0, FIELD_HEIGHT));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));

    this.selectedTime = new Date();

    // Create text field
    timeField = new JTextField();
    timeField.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
    timeField.setOpaque(false);
    timeField.setFont(timeField.getFont().deriveFont(Font.PLAIN, 14f));
    timeField.setEditable(false);
    if (placeholder != null) {
      timeField.putClientProperty("JTextField.placeholderText", placeholder);
    }

    // Create clock icon button
    JButton iconButton = createIconButton();

    add(timeField, BorderLayout.CENTER);
    add(iconButton, BorderLayout.EAST);

    // Add click listeners
    timeField.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        showTimePopup();
      }
    });
    iconButton.addActionListener(e -> showTimePopup());

    // Add focus listener for border repaint
    timeField.addFocusListener(new java.awt.event.FocusAdapter() {
      @Override
      public void focusGained(java.awt.event.FocusEvent e) {
        repaint();
      }

      @Override
      public void focusLost(java.awt.event.FocusEvent e) {
        repaint();
      }
    });
  }

  private JButton createIconButton() {
    JButton button = new JButton() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0x6B7280));

        int width = getWidth();
        int height = getHeight();
        int iconSize = 18;
        int x = (width - iconSize) / 2;
        int y = (height - iconSize) / 2;
        int centerX = x + iconSize / 2;
        int centerY = y + iconSize / 2;

        // Draw clock icon
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(x + 1, y + 1, iconSize - 2, iconSize - 2);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(centerX, centerY, centerX + 4, centerY);
        g2.drawLine(centerX, centerY, centerX, centerY - 5);
        g2.fillOval(centerX - 1, centerY - 1, 2, 2);

        g2.dispose();
      }
    };
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setOpaque(false);
    button.setPreferredSize(new Dimension(40, FIELD_HEIGHT));
    button.setFocusPainted(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return button;
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // White rounded background
    g2.setColor(Color.WHITE);
    g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);

    // Border
    boolean focused = timeField.isFocusOwner();
    g2.setColor(focused ? FOCUS_COLOR : BORDER_COLOR);
    g2.setStroke(new BasicStroke(1f));
    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

    g2.dispose();
    super.paintComponent(g);
  }

  private void showTimePopup() {
    JPopupMenu popup = new JPopupMenu();
    popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    popup.add(createTimeListPanel(popup));
    popup.setPopupSize(getWidth(), 300);
    popup.show(this, 0, getHeight());
  }

  private JPanel createTimeListPanel(JPopupMenu popup) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Get current time for highlighting
    Calendar cal = Calendar.getInstance();
    if (selectedTime != null) {
      cal.setTime(selectedTime);
    }
    int currentHour = cal.get(Calendar.HOUR_OF_DAY);
    int currentMinute = cal.get(Calendar.MINUTE);
    int currentTimeMinutes = currentHour * 60 + currentMinute;

    // Create scrollable list of time slots
    JPanel timeListPanel = new JPanel();
    timeListPanel.setLayout(new BoxLayout(timeListPanel, BoxLayout.Y_AXIS));
    timeListPanel.setBackground(Color.WHITE);

    // Find closest time slot
    int closestSlotMinutes = -1;
    int minDiff = Integer.MAX_VALUE;
    for (int hour = 0; hour < 24; hour++) {
      for (int minute = 0; minute < 60; minute += 30) {
        int slotMinutes = hour * 60 + minute;
        int diff = Math.abs(slotMinutes - currentTimeMinutes);
        if (diff < minDiff) {
          minDiff = diff;
          closestSlotMinutes = slotMinutes;
        }
      }
    }

    // Generate time slots in 30-minute intervals
    int slotIndex = 0;
    final int[] closestSlotIndex = { -1 };
    final int finalClosestSlot = closestSlotMinutes;

    for (int hour = 0; hour < 24; hour++) {
      for (int minute = 0; minute < 60; minute += 30) {
        final int h = hour;
        final int m = minute;
        String timeStr = String.format("%02d:%02d", hour, minute);
        int slotMinutes = hour * 60 + minute;
        boolean isSelected = (slotMinutes == finalClosestSlot);

        if (isSelected) {
          closestSlotIndex[0] = slotIndex;
        }

        JButton timeBtn = createTimeSlotButton(timeStr, isSelected);
        timeBtn.addActionListener(e -> {
          Calendar newCal = Calendar.getInstance();
          newCal.set(Calendar.HOUR_OF_DAY, h);
          newCal.set(Calendar.MINUTE, m);
          newCal.set(Calendar.SECOND, 0);
          setTime(newCal.getTime());
          popup.setVisible(false);
          if (onTimeSelected != null) {
            onTimeSelected.accept(selectedTime);
          }
        });
        timeListPanel.add(timeBtn);
        slotIndex++;
      }
    }

    JScrollPane scrollPane = new JScrollPane(timeListPanel);
    scrollPane.setBorder(null);
    scrollPane.setPreferredSize(new Dimension(200, 250));
    scrollPane.getViewport().setBackground(Color.WHITE);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    // Scroll to closest time slot
    if (closestSlotIndex[0] >= 0) {
      final int idx = closestSlotIndex[0];
      SwingUtilities.invokeLater(() -> {
        Component comp = timeListPanel.getComponent(idx);
        if (comp != null) {
          Rectangle rect = comp.getBounds();
          scrollPane.getViewport().scrollRectToVisible(rect);
        }
      });
    }

    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
  }

  private JButton createTimeSlotButton(String text, boolean isSelected) {
    JButton btn = new JButton(text) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected || getModel().isRollover()) {
          Color bgColor = isSelected ? SELECTED_BG : HOVER_BG;
          g2.setColor(bgColor);
          g2.fillRect(0, 0, getWidth(), getHeight());
          g2.setColor(isSelected ? Color.WHITE : Color.BLACK);
        } else {
          g2.setColor(Color.WHITE);
          g2.fillRect(0, 0, getWidth(), getHeight());
          g2.setColor(Color.BLACK);
        }

        FontMetrics fm = g2.getFontMetrics(getFont());
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
      }
    };
    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setPreferredSize(new Dimension(180, 35));
    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 13f));
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    return btn;
  }

  // Public API

  public void setTime(Date time) {
    this.selectedTime = time;
    if (time != null) {
      timeField.setText(timeFormat.format(time));
    } else {
      timeField.setText("");
    }
  }

  public Date getTime() {
    return selectedTime;
  }

  public String getText() {
    return timeField.getText();
  }

  public void setOnTimeSelected(Consumer<Date> listener) {
    this.onTimeSelected = listener;
  }

  public void clear() {
    selectedTime = null;
    timeField.setText("");
  }

  /**
   * Add a change listener that fires when the text field content changes.
   */
  public void addChangeListener(Runnable listener) {
    timeField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      @Override
      public void insertUpdate(javax.swing.event.DocumentEvent e) {
        listener.run();
      }

      @Override
      public void removeUpdate(javax.swing.event.DocumentEvent e) {
        listener.run();
      }

      @Override
      public void changedUpdate(javax.swing.event.DocumentEvent e) {
        listener.run();
      }
    });
  }

  /**
   * Get the underlying text field for additional customization.
   */
  public JTextField getTextField() {
    return timeField;
  }
}
