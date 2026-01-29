package com.expensemanager.view.CommonComponents;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Reusable Calendar Picker component.
 * Displays a popup calendar for date selection.
 */
public class CalendarPicker extends JPanel {

    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final Color FOCUS_COLOR = new Color(0x155DFC);
    private static final Color SELECTED_BG = new Color(0x155DFC);
    private static final Color HOVER_BG = new Color(0xE3F2FD);
    private static final int ARC = 30;
    private static final int FIELD_HEIGHT = 48;

    private final JTextField dateField;
    private Date selectedDate;
    private Consumer<Date> onDateSelected;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public CalendarPicker() {
        this(null);
    }

    public CalendarPicker(String placeholder) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(0, FIELD_HEIGHT));
        setMinimumSize(new Dimension(0, FIELD_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));

        this.selectedDate = new Date();

        // Create text field
        dateField = new JTextField();
        dateField.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        dateField.setOpaque(false);
        dateField.setFont(dateField.getFont().deriveFont(Font.PLAIN, 14f));
        dateField.setEditable(false);
        if (placeholder != null) {
            dateField.putClientProperty("JTextField.placeholderText", placeholder);
        }

        // Create calendar icon button
        JButton iconButton = createIconButton();

        add(dateField, BorderLayout.CENTER);
        add(iconButton, BorderLayout.EAST);

        // Add click listeners
        dateField.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showCalendarPopup();
            }
        });
        iconButton.addActionListener(e -> showCalendarPopup());

        // Add focus listener for border repaint
        dateField.addFocusListener(new java.awt.event.FocusAdapter() {
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
                int iconSize = 16;
                int x = (width - iconSize) / 2;
                int y = (height - iconSize) / 2;

                // Draw calendar icon
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x + 1, y + 3, iconSize - 2, iconSize - 4, 3, 3);
                g2.fillRect(x + 2, y + 3, iconSize - 4, 3);

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
        boolean focused = dateField.isFocusOwner();
        g2.setColor(focused ? FOCUS_COLOR : BORDER_COLOR);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

        g2.dispose();
        super.paintComponent(g);
    }

    private void showCalendarPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        popup.add(createCalendarPanel(popup));
        popup.setPopupSize(getWidth(), 350);
        popup.show(this, 0, getHeight());
    }

    private JPanel createCalendarPanel(JPopupMenu popup) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate != null ? selectedDate : new Date());
        final int[] currentYear = { cal.get(Calendar.YEAR) };
        final int[] currentMonth = { cal.get(Calendar.MONTH) };

        // Header with navigation
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        final JLabel monthYearLabel = new JLabel();
        monthYearLabel.setFont(monthYearLabel.getFont().deriveFont(Font.BOLD, 14f));
        monthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);

        final JPanel[] bodyPanelRef = { new JPanel(new GridLayout(0, 7, 5, 5)) };
        bodyPanelRef[0].setBackground(Color.WHITE);

        Runnable buildBody = () -> {
            bodyPanelRef[0].removeAll();

            // Day headers
            String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
            for (String dayName : dayNames) {
                JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
                dayLabel.setFont(dayLabel.getFont().deriveFont(Font.PLAIN, 11f));
                dayLabel.setForeground(new Color(0x6B7280));
                bodyPanelRef[0].add(dayLabel);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(currentYear[0], currentMonth[0], 1);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Get selected date for highlighting
            Calendar selectedCal = Calendar.getInstance();
            if (selectedDate != null) {
                selectedCal.setTime(selectedDate);
            }
            int selectedYear = selectedCal.get(Calendar.YEAR);
            int selectedMonth = selectedCal.get(Calendar.MONTH);
            int selectedDay = selectedCal.get(Calendar.DAY_OF_MONTH);

            // Empty cells before first day
            for (int i = 0; i < firstDayOfWeek; i++) {
                bodyPanelRef[0].add(new JLabel());
            }

            // Day buttons
            for (int day = 1; day <= daysInMonth; day++) {
                final int dayValue = day;
                boolean isSelected = (currentYear[0] == selectedYear &&
                        currentMonth[0] == selectedMonth &&
                        day == selectedDay);
                JButton dayBtn = createDayButton(String.valueOf(day), isSelected);
                dayBtn.addActionListener(e -> {
                    Calendar newCal = Calendar.getInstance();
                    newCal.set(currentYear[0], currentMonth[0], dayValue);
                    setDate(newCal.getTime());
                    popup.setVisible(false);
                    if (onDateSelected != null) {
                        onDateSelected.accept(selectedDate);
                    }
                });
                bodyPanelRef[0].add(dayBtn);
            }

            // Update header label
            String[] monthNames = { "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December" };
            monthYearLabel.setText(monthNames[currentMonth[0]] + " " + currentYear[0]);

            bodyPanelRef[0].revalidate();
            bodyPanelRef[0].repaint();
        };

        // Previous/Next buttons
        JButton prevBtn = createNavButton("<");
        prevBtn.addActionListener(e -> {
            currentMonth[0]--;
            if (currentMonth[0] < 0) {
                currentMonth[0] = 11;
                currentYear[0]--;
            }
            buildBody.run();
        });

        JButton nextBtn = createNavButton(">");
        nextBtn.addActionListener(e -> {
            currentMonth[0]++;
            if (currentMonth[0] > 11) {
                currentMonth[0] = 0;
                currentYear[0]++;
            }
            buildBody.run();
        });

        headerPanel.add(prevBtn, BorderLayout.WEST);
        headerPanel.add(monthYearLabel, BorderLayout.CENTER);
        headerPanel.add(nextBtn, BorderLayout.EAST);

        buildBody.run();

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(bodyPanelRef[0], BorderLayout.CENTER);

        return panel;
    }

    private JButton createDayButton(String text, boolean isSelected) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected || getModel().isRollover()) {
                    g2.setColor(isSelected ? SELECTED_BG : HOVER_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                    g2.setColor(isSelected ? Color.WHITE : Color.BLACK);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
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
        btn.setPreferredSize(new Dimension(35, 35));
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 12f));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(HOVER_BG);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Color.BLACK);
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
        btn.setPreferredSize(new Dimension(30, 30));
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 14f));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Public API

    public void setDate(Date date) {
        this.selectedDate = date;
        if (date != null) {
            dateField.setText(dateFormat.format(date));
        } else {
            dateField.setText("");
        }
    }

    public Date getDate() {
        return selectedDate;
    }

    public String getText() {
        return dateField.getText();
    }

    public void setOnDateSelected(Consumer<Date> listener) {
        this.onDateSelected = listener;
    }

    public void clear() {
        selectedDate = null;
        dateField.setText("");
    }
}
