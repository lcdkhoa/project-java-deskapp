package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.util.MonthKeyUtil;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unified Add/Edit Budget dialog with modern rounded inputs.
 */
public class BudgetDialog extends JDialog {

    public enum Mode {
        ADD, EDIT
    }

    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final Color PRIMARY_COLOR = new Color(0x2563EB);
    private static final int CARD_ARC = 30;
    private static final int CONTROL_HEIGHT = 48;

    private final MainFrame main;
    private final YearMonth month;
    private final Mode mode;
    private final Budget existing;
    @SuppressWarnings("unused")
    private final Category existingCategory;

    private JComboBox<Category> categoryCombo;
    private JTextField amountField;

    public BudgetDialog(MainFrame main, YearMonth month, Mode mode, Budget existing, Category existingCategory) {
        super(main, mode == Mode.ADD ? "Add Budget" : "Edit Budget", true);
        this.main = main;
        this.month = month;
        this.mode = mode;
        this.existing = existing;
        this.existingCategory = existingCategory;

        setSize(460, 320);
        setLocationRelativeTo(main);
        setLayout(new BorderLayout());

        // Row constraints: label rows min 20, input rows 48 so amount field is never
        // squeezed
        JPanel form = new JPanel(new MigLayout("ins 20 20 16 20, wrap 1, gapy 16",
                "[grow]", "[20!][48!][20!][48!]"));
        form.setBackground(Color.WHITE);

        // Category
        JLabel catLabel = new JLabel(mode == Mode.ADD ? "Category *" : "Category");
        catLabel.setFont(catLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(catLabel, "alignx left");

        if (mode == Mode.ADD) {
            categoryCombo = createStyledCategoryCombo();
            refillCategories();
            form.add(categoryCombo, "growx, h 48!");
        } else {
            // Read-only category field styled like textfield
            String name;
            if (existingCategory != null) {
                name = existingCategory.getName();
            } else {
                name = "";
            }
            JTextField readOnly = createStyledTextField();
            readOnly.setText(name);
            readOnly.setEditable(false);
            readOnly.setEnabled(false);
            form.add(readOnly, "growx, h 48!");
        }

        // Amount
        JLabel amountLabel = new JLabel("Monthly Budget Amount * (max 500.000.000 đ)");
        amountLabel.setFont(amountLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(amountLabel, "alignx left");

        amountField = createStyledTextField();
        amountField.setPreferredSize(new Dimension(0, CONTROL_HEIGHT));
        amountField.setMinimumSize(new Dimension(120, CONTROL_HEIGHT));
        if (existing != null) {
            amountField.setText(String.valueOf(existing.getAmount()));
        }
        form.add(amountField, "growx, h 48!, wmin 120");

        add(form, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new MigLayout("ins 0 20 20 20, gap 10", "[grow][grow]", "[]"));
        footer.setBackground(Color.WHITE);

        JButton cancel = createFooterButton("Cancel", false);
        cancel.addActionListener(e -> dispose());
        footer.add(cancel, "growx, h 48!");

        JButton save = mode == Mode.ADD ? createFooterButton("Add Budget", true)
                : createFooterButton("Update Budget", true);
        save.addActionListener(e -> onSave());
        footer.add(save, "growx, h 48!");

        add(footer, BorderLayout.SOUTH);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 14f));
        return field;
    }

    /**
     * Category dropdown styled like TransactionView createStyledComboBox:
     * white bg, rounded (CARD_ARC), border blue on focus/popup, custom arrow, 48px
     * height.
     */
    private JComboBox<Category> createStyledCategoryCombo() {
        JComboBox<Category> combo = new JComboBox<Category>() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
                g2.dispose();
                super.paintComponent(g);
                Graphics2D g2Border = (Graphics2D) g.create();
                g2Border.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color borderColor = (isFocusOwner() || isPopupVisible()) ? new Color(0x155DFC) : BORDER_COLOR;
                g2Border.setColor(borderColor);
                g2Border.setStroke(new BasicStroke(1));
                g2Border.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CARD_ARC, CARD_ARC);
                g2Border.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Border is painted inside paintComponent.
            }
        };
        combo.setOpaque(false);
        combo.setPreferredSize(new Dimension(0, CONTROL_HEIGHT));
        combo.setMinimumSize(new Dimension(0, CONTROL_HEIGHT));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTROL_HEIGHT));
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
                    button.setPreferredSize(new Dimension(40, CONTROL_HEIGHT));
                    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    return button;
                }
            });
        } catch (Exception ignored) {
        }

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                }
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

    private JButton createFooterButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(primary ? PRIMARY_COLOR : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                if (!primary) {
                    g2.setColor(BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                }

                g2.setColor(primary ? Color.WHITE : Color.BLACK);
                Font font = getFont().deriveFont(primary ? Font.BOLD : Font.PLAIN, 14f);
                g2.setFont(font);
                FontMetrics fm = g2.getFontMetrics(font);
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }

    private void refillCategories() {
        categoryCombo.removeAllItems();
        String monthKey = MonthKeyUtil.of(month);
        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Budget> existing = new BudgetDAO().findByUserAndMonth(conn, AppContext.getUserId(), monthKey);
            Set<String> having = new HashSet<>();
            for (Budget b : existing) {
                having.add(b.getCategoryId());
            }
            List<Category> list = new CategoryDAO().findByType(conn, "expense");
            for (Category c : list) {
                if (!having.contains(c.getId())) {
                    categoryCombo.addItem(c);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void onSave() {
        String rawAmount = amountField.getText() != null ? amountField.getText().trim().replaceAll("[.,\\s]", "") : "";
        if (rawAmount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount is required.");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(rawAmount);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
            return;
        }
        if (amount <= 0 || amount > 500_000_000L) {
            JOptionPane.showMessageDialog(this, "Amount must be 1..500.000.000.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            BudgetDAO dao = new BudgetDAO();
            if (mode == Mode.ADD) {
                Category c = (Category) categoryCombo.getSelectedItem();
                if (c == null) {
                    JOptionPane.showMessageDialog(this, "Select a category.");
                    return;
                }
                Budget b = new Budget();
                b.setUserId(AppContext.getUserId());
                b.setCategoryId(c.getId());
                b.setMonthKey(MonthKeyUtil.of(month));
                b.setAmount(amount);
                dao.insert(conn, b);
            } else {
                existing.setAmount(amount);
                dao.update(conn, existing);
            }
            main.refreshBudget();
            main.refreshDashboard();
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }
}
