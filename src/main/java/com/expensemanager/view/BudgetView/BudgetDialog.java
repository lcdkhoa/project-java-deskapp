package com.expensemanager.view.BudgetView;

import com.expensemanager.util.AppContext;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.view.CommonComponents.MainFrame;
import com.expensemanager.model.Budget;
import com.expensemanager.model.Category;
import com.expensemanager.util.MonthKeyUtil;
import com.expensemanager.view.CommonComponents.StyledComponents;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;
import java.util.List;

public class BudgetDialog extends JDialog {

    public enum Mode {
        ADD, EDIT
    }

    private static final int CARD_ARC = 30;
    private static final int CONTROL_HEIGHT = 48;

    private final YearMonth month;
    private final Mode mode;
    private final Budget existing;
    private final BudgetDialogListener listener;

    private JComboBox<Category> categoryCombo;
    private JTextField amountField;

    public BudgetDialog(MainFrame main, YearMonth month, Mode mode, Budget existing,
            Category existingCategory, BudgetDialogListener listener) {
        super(main, mode == Mode.ADD ? "Add Budget" : "Edit Budget", true);
        this.month = month;
        this.mode = mode;
        this.existing = existing;
        this.listener = listener;

        setSize(460, 320);
        setLocationRelativeTo(main);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new MigLayout("ins 20 20 16 20, wrap 1, gapy 16",
                "[grow]", "[20!][48!][20!][48!]"));
        form.setBackground(Color.WHITE);

        JLabel catLabel = new JLabel(mode == Mode.ADD ? "Category *" : "Category");
        catLabel.setFont(catLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(catLabel, "alignx left");

        if (mode == Mode.ADD) {
            categoryCombo = createStyledCategoryCombo();
            refillCategories();
            form.add(categoryCombo, "growx, h 48!");
        } else {
            String name = existingCategory != null ? existingCategory.getName() : "";
            JTextField readOnly = createStyledTextField();
            readOnly.setText(name);
            readOnly.setEditable(false);
            readOnly.setEnabled(false);
            form.add(readOnly, "growx, h 48!");
        }

        JLabel amountLabel = new JLabel("Monthly Budget Amount * (max 500.000.000 đ)");
        amountLabel.setFont(amountLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(amountLabel, "alignx left");

        amountField = createStyledTextField();
        amountField.setPreferredSize(new Dimension(0, CONTROL_HEIGHT));
        amountField.setMinimumSize(new Dimension(120, CONTROL_HEIGHT));
        CurrencyUtil.applyThousandSeparator(amountField);
        if (existing != null) {
            amountField.setText(CurrencyUtil.formatNoSymbol(existing.getAmount()));
        }
        form.add(amountField, "growx, h 48!, wmin 120");

        add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new MigLayout("ins 0 20 20 20, gap 10", "[grow][grow]", "[]"));
        footer.setBackground(Color.WHITE);

        JButton cancel = StyledComponents.createSecondaryFunctionButton("Cancel", 0);
        cancel.addActionListener(e -> dispose());
        footer.add(cancel, "growx, h 48!");

        String saveText = mode == Mode.ADD ? "Add Budget" : "Update Budget";
        JButton save = StyledComponents.createPrimaryFunctionButton(saveText, 0);
        save.addActionListener(e -> onSave());
        footer.add(save, "growx, h 48!");

        add(footer, BorderLayout.SOUTH);
    }

    private JTextField createStyledTextField() {
        return StyledComponents.createStyledTextField();
    }

    private JComboBox<Category> createStyledCategoryCombo() {
        JComboBox<Category> combo = StyledComponents.createStyledComboBox(CONTROL_HEIGHT, CARD_ARC);
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
        return combo;
    }

    private void refillCategories() {
        categoryCombo.removeAllItems();
        List<Category> available = listener.getAvailableCategories();
        for (Category c : available) {
            categoryCombo.addItem(c);
        }
    }

    private void onSave() {
        String rawAmount = CurrencyUtil.parseRawNumber(amountField.getText());
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

        try {
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
                listener.onBudgetCreated(b);
            } else {
                existing.setAmount(amount);
                listener.onBudgetUpdated(existing);
            }
            listener.onRefreshRequired();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }
}
