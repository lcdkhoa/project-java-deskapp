package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.util.MonthKeyUtil;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create Transaction modal - Section 2.1. Amount, Type, Date, Time, Category,
 * Wallet (optional), Note (max 120).
 * Expense → amount < 0; Income → amount > 0.
 * Refactored with MigLayout and new Figma design specs.
 */
public class CreateTransactionDialog extends JDialog {
    private final MainFrame main;
    private JTextField amountF;
    private JToggleButton expenseBtn;
    private JToggleButton incomeBtn;
    private ButtonGroup typeGroup;
    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private JComboBox<String> walletCombo;
    private JTextField noteF;

    // Category grid
    private Map<String, CategoryItemPanel> categoryPanels;
    private CategoryItemPanel selectedCategoryPanel;
    private JPanel categoryGridPanel;

    // Category name to icon file mapping
    private static final Map<String, String> CATEGORY_ICON_MAP = new HashMap<>();
    static {
        // Expense categories
        CATEGORY_ICON_MAP.put("Food", "imgs/category/food.png");
        CATEGORY_ICON_MAP.put("Transport", "imgs/category/transport.png");
        CATEGORY_ICON_MAP.put("Shopping", "imgs/category/shopping.png");
        CATEGORY_ICON_MAP.put("Entertainment", "imgs/category/entertainment.png");
        CATEGORY_ICON_MAP.put("Bills", "imgs/category/bill.png");
        CATEGORY_ICON_MAP.put("Healthcare", "imgs/category/healthcare.png");
        CATEGORY_ICON_MAP.put("Housing", "imgs/category/housing.png");
        CATEGORY_ICON_MAP.put("Education", "imgs/category/education.png");
        CATEGORY_ICON_MAP.put("Other", "imgs/category/others.png");

        // Income categories - placeholder paths (will show "?" if files don't exist)
        CATEGORY_ICON_MAP.put("Salary", "imgs/category/salary.png");
        CATEGORY_ICON_MAP.put("Freelance", "imgs/category/freelance.png");
        CATEGORY_ICON_MAP.put("Affiliate", "imgs/category/affiliate.png");
        CATEGORY_ICON_MAP.put("Selling", "imgs/category/selling.png");
        CATEGORY_ICON_MAP.put("Other Income", "imgs/category/other_income.png");
    }

    private static final String[] WALLET_OPTIONS = { "Cash", "Bank Transfer", "Momo", "ZaloPay" };
    private static final String[] WALLET_VALUES = { "cash", "bank_transfer", "momo", "zalopay" };

    private static final Color EXPENSE_COLOR = new Color(0xE7000B);
    private static final Color INCOME_COLOR = new Color(0x00A63E);
    private static final Color INACTIVE_BG = new Color(0xF3F4F6);
    private static final Color SAVE_BUTTON_COLOR = new Color(0x155DFC);

    public CreateTransactionDialog(MainFrame main) {
        super(main, "Create Transaction", true);
        this.main = main;
        this.categoryPanels = new HashMap<>();

        setSize(510, 700);
        setLocationRelativeTo(main);
        setLayout(new BorderLayout());

        // Main form panel with MigLayout - width 510px, components centered
        // Content width = 510 - 40 (padding) = 470px
        JPanel form = new JPanel(new MigLayout("ins 20, wrap 1, gapy 15, align center", "[450!]", "[]"));
        form.setBackground(Color.WHITE);

        // Amount field - width 452px, height 60px, centered
        JLabel amountLabel = new JLabel("Amount *");
        amountLabel.setFont(amountLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(amountLabel, "alignx left");

        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.setOpaque(false);
        amountF = createStyledTextField(452, 60, 30);
        amountF.setFont(amountF.getFont().deriveFont(Font.PLAIN, 16f));
        amountPanel.add(amountF, BorderLayout.CENTER);

        JLabel vndLabel = new JLabel("VND");
        vndLabel.setForeground(new Color(0x9CA3AF));
        vndLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));
        amountPanel.add(vndLabel, BorderLayout.EAST);
        form.add(amountPanel, "w 452!, alignx center, wrap");

        // Transaction Type Toggle Buttons - each 220px, gap 10px, centered
        JLabel typeLabel = new JLabel("Type *");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(typeLabel, "alignx left");

        JPanel typePanel = createTypeTogglePanel();
        form.add(typePanel, "w 450!, alignx center, wrap");

        // Date & Time (side by side) - centered
        JLabel dateTimeLabel = new JLabel("Date & Time *");
        dateTimeLabel.setFont(dateTimeLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(dateTimeLabel, "alignx left");

        JPanel dateTimePanel = new JPanel(new MigLayout("ins 0, gap 10", "[grow,fill][grow,fill]", "[]"));
        dateTimePanel.setOpaque(false);

        dateSpinner = createDateSpinner();
        dateTimePanel.add(createDateField(dateSpinner), "growx");

        timeSpinner = createTimeSpinner();
        dateTimePanel.add(createTimeField(timeSpinner), "growx");

        form.add(dateTimePanel, "w 452!, alignx center, wrap");

        // Category Grid - centered
        JLabel categoryLabel = new JLabel("Category *");
        categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(categoryLabel, "alignx left");

        categoryGridPanel = new JPanel(new MigLayout("ins 0, gap 10 10", "[140!][140!][140!]", "[75!][75!][75!]"));
        categoryGridPanel.setOpaque(false);
        form.add(categoryGridPanel, "w 450!, alignx center, wrap");

        // Wallet - width 452px, height 48px, centered
        JLabel walletLabel = new JLabel("Wallet");
        walletLabel.setFont(walletLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(walletLabel, "alignx left");

        walletCombo = createStyledComboBox(452, 48, 30);
        walletCombo.setModel(new DefaultComboBoxModel<>(WALLET_OPTIONS));
        form.add(walletCombo, "w 452!, alignx center, wrap");

        // Note
        JLabel noteLabel = new JLabel("Note");
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(noteLabel, "alignx left");

        noteF = createStyledTextField(452, 48, 30);
        form.add(noteF, "w 452!, alignx center, wrap");

        // Footer Buttons - each 220px, gap 10px, centered
        JPanel buttonPanel = new JPanel(new MigLayout("ins 0, gap 10", "[220!][220!]", "[]"));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = createFooterButton("Cancel", false);
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn, "w 220!");

        JButton saveBtn = createFooterButton("Save", true);
        saveBtn.addActionListener(e -> onSave());
        buttonPanel.add(saveBtn, "w 220!");

        form.add(buttonPanel, "w 450!, alignx center, wrap");

        // Add form to dialog
        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize with Expense selected and load categories
        expenseBtn.setSelected(true);
        updateAmountColor();
        refillCategories();
    }

    private JPanel createTypeTogglePanel() {
        // Two buttons: each 220px width, gap 10px, total 450px
        JPanel panel = new JPanel(new MigLayout("ins 0, gap 10", "[220!][220!]", "[]"));
        panel.setOpaque(false);

        typeGroup = new ButtonGroup();

        expenseBtn = createToggleButton("Expense", EXPENSE_COLOR);
        incomeBtn = createToggleButton("Income", INCOME_COLOR);

        typeGroup.add(expenseBtn);
        typeGroup.add(incomeBtn);

        expenseBtn.addActionListener(e -> {
            updateAmountColor();
            refillCategories();
        });
        incomeBtn.addActionListener(e -> {
            updateAmountColor();
            refillCategories();
        });

        panel.add(expenseBtn, "w 220!");
        panel.add(incomeBtn, "w 220!");

        return panel;
    }

    private JToggleButton createToggleButton(String text, Color activeColor) {
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill white background first to avoid gray showing through rounded corners
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                if (isSelected()) {
                    g2.setColor(activeColor);
                } else {
                    g2.setColor(INACTIVE_BG);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.setColor(isSelected() ? Color.WHITE : Color.BLACK);
                FontMetrics fm = g2.getFontMetrics(getFont());
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(220, 48));
        btn.setMinimumSize(new Dimension(220, 48));
        btn.setMaximumSize(new Dimension(220, 48));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 14f));

        return btn;
    }

    private void updateAmountColor() {
        if (expenseBtn.isSelected()) {
            amountF.setForeground(EXPENSE_COLOR);
        } else if (incomeBtn.isSelected()) {
            amountF.setForeground(INCOME_COLOR);
        }
    }

    private JPanel createDateField(JSpinner spinner) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fill white background with rounded corners - ensure no gray
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                // Draw border only
                g2.setColor(new Color(0xE5E7EB));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(221, 48));
        panel.setMinimumSize(new Dimension(221, 48));
        panel.setMaximumSize(new Dimension(221, 48));

        // Calendar icon on right
        JLabel iconLabel = new JLabel("📅");
        iconLabel.setOpaque(false);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Wrap spinner in a panel to style it and hide arrows
        JPanel spinnerWrapper = new JPanel(new BorderLayout());
        spinnerWrapper.setOpaque(false);
        spinnerWrapper.setBackground(Color.WHITE);
        spinnerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        spinnerWrapper.add(spinner, BorderLayout.CENTER);

        // Hide spinner arrows
        hideSpinnerArrows(spinner);

        panel.add(spinnerWrapper, BorderLayout.CENTER);
        panel.add(iconLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTimeField(JSpinner spinner) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fill white background with rounded corners - ensure no gray
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                // Draw border only
                g2.setColor(new Color(0xE5E7EB));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(221, 48));
        panel.setMinimumSize(new Dimension(221, 48));
        panel.setMaximumSize(new Dimension(221, 48));

        // Clock icon on right
        JLabel iconLabel = new JLabel("🕐");
        iconLabel.setOpaque(false);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Wrap spinner in a panel to style it and hide arrows
        JPanel spinnerWrapper = new JPanel(new BorderLayout());
        spinnerWrapper.setOpaque(false);
        spinnerWrapper.setBackground(Color.WHITE);
        spinnerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        spinnerWrapper.add(spinner, BorderLayout.CENTER);

        // Hide spinner arrows
        hideSpinnerArrows(spinner);

        panel.add(spinnerWrapper, BorderLayout.CENTER);
        panel.add(iconLabel, BorderLayout.EAST);

        return panel;
    }

    private void styleSpinnerField(JSpinner spinner) {
        try {
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
            if (editor != null) {
                JTextField textField = editor.getTextField();
                if (textField != null) {
                    textField.setOpaque(false);
                    textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    textField.setFont(textField.getFont().deriveFont(Font.PLAIN, 14f));
                }
            }
        } catch (Exception e) {
            // Ignore if editor is not ready yet
        }
    }

    private void hideSpinnerArrows(JSpinner spinner) {
        // Hide spinner arrows by finding and hiding the arrow buttons
        spinner.addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && spinner.isShowing()) {
                    SwingUtilities.invokeLater(() -> {
                        // Hide all buttons in spinner
                        hideButtonsRecursively(spinner);
                    });
                }
            }
        });
        // Also try immediately if already showing
        if (spinner.isShowing()) {
            SwingUtilities.invokeLater(() -> hideButtonsRecursively(spinner));
        }
    }

    private void hideButtonsRecursively(Container container) {
        Component[] comps = container.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JButton) {
                comp.setVisible(false);
                comp.setPreferredSize(new Dimension(0, 0));
            } else if (comp instanceof Container) {
                hideButtonsRecursively((Container) comp);
            }
        }
    }

    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "MM/dd/yyyy"));
        spinner.setPreferredSize(new Dimension(221, 48));
        spinner.setMaximumSize(new Dimension(221, 48));

        // Style the spinner after it's added to the container
        spinner.addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && spinner.isShowing()) {
                    SwingUtilities.invokeLater(() -> styleSpinnerField(spinner));
                }
            }
        });

        return spinner;
    }

    private JSpinner createTimeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "hh:mm a"));
        spinner.setPreferredSize(new Dimension(221, 48));
        spinner.setMaximumSize(new Dimension(221, 48));

        // Style the spinner after it's added to the container
        spinner.addHierarchyListener(new java.awt.event.HierarchyListener() {
            @Override
            public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && spinner.isShowing()) {
                    SwingUtilities.invokeLater(() -> styleSpinnerField(spinner));
                }
            }
        });

        return spinner;
    }

    private JTextField createStyledTextField(int width, int height, int arc) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fill white background with rounded corners - ensure no gray shows through
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                // Draw border only
                g2.setColor(new Color(0xE5E7EB));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(width, height));
        field.setMinimumSize(new Dimension(width, height));
        field.setMaximumSize(new Dimension(width, height));
        field.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 14f));
        return field;
    }

    private JComboBox<String> createStyledComboBox(int width, int height, int arc) {
        JComboBox<String> combo = new JComboBox<String>() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fill white background with rounded corners
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                // Draw border - blue when focused, gray otherwise
                Color borderColor = (isFocusOwner() || isPopupVisible()) ? new Color(0x155DFC) : new Color(0xE5E7EB);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Override to prevent default border painting
            }
        };
        combo.setOpaque(false);
        combo.setPreferredSize(new Dimension(width, height));
        combo.setMinimumSize(new Dimension(width, height));
        combo.setMaximumSize(new Dimension(width, height));
        combo.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        combo.setFont(combo.getFont().deriveFont(Font.PLAIN, 14f));

        // Hide arrow button by setting UI and customizing arrow button
        try {
            combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton button = new JButton() {
                        @Override
                        public void paintComponent(Graphics g) {
                            // Do nothing - hide the arrow
                        }
                    };
                    button.setVisible(false);
                    button.setPreferredSize(new Dimension(0, 0));
                    button.setMaximumSize(new Dimension(0, 0));
                    button.setMinimumSize(new Dimension(0, 0));
                    return button;
                }
            });
        } catch (Exception e) {
            // Fallback: hide arrow button after UI is set
            combo.addHierarchyListener(new java.awt.event.HierarchyListener() {
                @Override
                public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                    if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                            && combo.isShowing()) {
                        SwingUtilities.invokeLater(() -> {
                            // Find and hide arrow button
                            Component[] comps = combo.getComponents();
                            for (Component comp : comps) {
                                if (comp instanceof JButton) {
                                    comp.setVisible(false);
                                    comp.setPreferredSize(new Dimension(0, 0));
                                }
                            }
                        });
                    }
                }
            });
        }

        // Add focus listener to update border color when focus changes
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

        // Ensure renderer and editor also have white background
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(Color.WHITE);
                if (isSelected) {
                    c.setBackground(new Color(0x155DFC));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        return combo;
    }

    private JButton createFooterButton(String text, boolean isSave) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fill white background first to avoid gray showing through rounded corners
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                if (isSave) {
                    g2.setColor(SAVE_BUTTON_COLOR);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                if (!isSave) {
                    g2.setColor(new Color(0xE5E7EB));
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                }

                g2.setColor(isSave ? Color.WHITE : Color.BLACK);
                Font font = getFont().deriveFont(isSave ? Font.BOLD : Font.PLAIN, 14f);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics(font);
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(220, 48));
        btn.setMinimumSize(new Dimension(220, 48));
        btn.setMaximumSize(new Dimension(220, 48));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        return btn;
    }

    private void refillCategories() {
        // Clear existing panels
        categoryGridPanel.removeAll();
        categoryPanels.clear();
        selectedCategoryPanel = null;

        String type = expenseBtn.isSelected() ? "expense" : "income";

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Category> list = new CategoryDAO().findByType(conn, type);

            // Define category order based on type
            String[] categoryOrder;
            if (type.equals("expense")) {
                // Expense categories: Food, Transport, Shopping, Entertainment, Bills,
                // Healthcare,
                // Housing, Education, Other
                categoryOrder = new String[] { "Food", "Transport", "Shopping", "Entertainment", "Bills", "Healthcare",
                        "Housing", "Education", "Other" };
            } else {
                // Income categories: Salary, Freelance, Affiliate, Selling, Other Income
                categoryOrder = new String[] { "Salary", "Freelance", "Affiliate", "Selling", "Other Income" };
            }

            // Count valid categories
            int validCategoryCount = 0;
            for (String catName : categoryOrder) {
                Category category = list.stream()
                        .filter(c -> c.getName().equals(catName))
                        .findFirst()
                        .orElse(null);
                if (category != null) {
                    validCategoryCount++;
                }
            }

            // Calculate number of rows needed (3 columns per row)
            int rows = (int) Math.ceil(validCategoryCount / 3.0);
            if (rows == 0) rows = 1; // At least 1 row

            // Update categoryGridPanel layout with dynamic row count
            StringBuilder rowConstraints = new StringBuilder();
            for (int i = 0; i < rows; i++) {
                if (i > 0) rowConstraints.append(" ");
                rowConstraints.append("[75!]");
            }
            categoryGridPanel.setLayout(new MigLayout("ins 0, gap 10 10", "[140!][140!][140!]", rowConstraints.toString()));

            int index = 0;
            for (String catName : categoryOrder) {
                Category category = list.stream()
                        .filter(c -> c.getName().equals(catName))
                        .findFirst()
                        .orElse(null);

                if (category != null) {
                    // Get icon path from map, fallback to placeholder if not found
                    String iconPath = CATEGORY_ICON_MAP.getOrDefault(catName, "imgs/category/others.png");
                    CategoryItemPanel panel = new CategoryItemPanel(category.getId(), category.getName(), iconPath);

                    // Add click listener
                    panel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            // Deselect previous
                            if (selectedCategoryPanel != null) {
                                selectedCategoryPanel.setSelected(false);
                            }
                            // Select current
                            panel.setSelected(true);
                            selectedCategoryPanel = panel;
                        }
                    });

                    categoryPanels.put(category.getId(), panel);
                    categoryGridPanel.add(panel, "cell " + (index % 3) + " " + (index / 3));
                    index++;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + ex.getMessage());
        }

        categoryGridPanel.revalidate();
        categoryGridPanel.repaint();
    }

    private void onSave() {
        String amountS = amountF.getText().trim();
        if (amountS.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount is required.");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(amountS.replaceAll("[.,\\s]", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
            return;
        }
        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be positive.");
            return;
        }

        String type = expenseBtn.isSelected() ? "expense" : "income";
        if (type.equals("expense"))
            amount = -amount;

        if (selectedCategoryPanel == null) {
            JOptionPane.showMessageDialog(this, "Select a category.");
            return;
        }
        String categoryId = selectedCategoryPanel.getCategoryId();

        LocalDate d = ((Date) dateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime t = ((Date) timeSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        String wallet = WALLET_VALUES[walletCombo.getSelectedIndex()];
        String note = noteF.getText();
        if (note != null && note.length() > 120)
            note = note.substring(0, 120);

        Transaction tx = new Transaction();
        tx.setUserId(AppContext.getUserId());
        tx.setAmount(amount);
        tx.setType(type);
        tx.setCategoryId(categoryId);
        tx.setWalletType(wallet);
        tx.setNote(note.isEmpty() ? null : note);
        tx.setTransactionDate(d);
        tx.setTransactionTime(t);
        tx.setMonthKey(MonthKeyUtil.of(d));

        try (Connection conn = DatabaseConnection.getConnection()) {
            new TransactionDAO().insert(conn, tx);
            main.refreshDashboard();
            main.refreshTransactions();
            main.refreshBudget();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }
}
