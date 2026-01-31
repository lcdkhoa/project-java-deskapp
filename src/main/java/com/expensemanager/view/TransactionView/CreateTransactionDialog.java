package com.expensemanager.view.TransactionView;

import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.WalletType;
import com.expensemanager.util.MonthKeyUtil;
import com.expensemanager.view.CommonComponents.CalendarPicker;
import com.expensemanager.view.CommonComponents.MainFrame;
import com.expensemanager.view.CommonComponents.StyledComponents;
import com.expensemanager.view.CommonComponents.TimePicker;
import com.expensemanager.view.CommonComponents.WalletListCellRenderer;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateTransactionDialog extends JDialog {
    private final TransactionDialogListener listener;
    private JTextField amountF;
    private JToggleButton expenseBtn;
    private JToggleButton incomeBtn;
    private ButtonGroup typeGroup;
    private CalendarPicker datePicker;
    private TimePicker timePicker;
    private JComboBox<WalletType> walletCombo;
    private JTextField noteF;

    private Map<String, CategoryItemPanel> categoryPanels;
    private CategoryItemPanel selectedCategoryPanel;
    private JPanel categoryGridPanel;

    private List<WalletType> walletTypes;

    private static final Color EXPENSE_COLOR = new Color(0xE7000B);
    private static final Color INCOME_COLOR = new Color(0x00A63E);

    public CreateTransactionDialog(MainFrame main, TransactionDialogListener listener) {
        super(main, "Create Transaction", true);
        this.listener = listener;
        this.categoryPanels = new HashMap<>();
        this.walletTypes = listener.getWalletTypes();

        setSize(510, 700);
        setLocationRelativeTo(main);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new MigLayout("ins 20, wrap 1, gapy 15, align center", "[450!]", "[]"));
        form.setBackground(Color.WHITE);

        JLabel amountLabel = new JLabel("Amount *");
        amountLabel.setFont(amountLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(amountLabel, "alignx left");

        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.setOpaque(false);
        amountF = StyledComponents.createStyledTextField(452, 60, 30);
        amountF.setFont(amountF.getFont().deriveFont(Font.PLAIN, 16f));
        CurrencyUtil.applyThousandSeparator(amountF);
        amountPanel.add(amountF, BorderLayout.CENTER);

        JLabel vndLabel = new JLabel("VND");
        vndLabel.setForeground(new Color(0x9CA3AF));
        vndLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));
        amountPanel.add(vndLabel, BorderLayout.EAST);
        form.add(amountPanel, "w 452!, alignx center, wrap");

        JLabel typeLabel = new JLabel("Type *");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(typeLabel, "alignx left");

        JPanel typePanel = createTypeTogglePanel();
        form.add(typePanel, "w 450!, alignx center, wrap");

        JLabel dateTimeLabel = new JLabel("Date & Time *");
        dateTimeLabel.setFont(dateTimeLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(dateTimeLabel, "alignx left");

        JPanel dateTimePanel = new JPanel(new MigLayout("ins 0, gap 10", "[grow,fill][grow,fill]", "[]"));
        dateTimePanel.setOpaque(false);

        datePicker = new CalendarPicker("mm/dd/yyyy");
        timePicker = new TimePicker("hh:mm a");

        datePicker.setDate(new Date());
        timePicker.setTime(new Date());

        dateTimePanel.add(datePicker, "growx");
        dateTimePanel.add(timePicker, "growx");

        form.add(dateTimePanel, "w 452!, alignx center, wrap");

        JLabel categoryLabel = new JLabel("Category *");
        categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(categoryLabel, "alignx left");

        categoryGridPanel = new JPanel(new MigLayout("ins 0, gap 10 10", "[140!][140!][140!]", "[75!][75!][75!]"));
        categoryGridPanel.setOpaque(false);
        form.add(categoryGridPanel, "w 450!, alignx center, wrap");

        JLabel walletLabel = new JLabel("Wallet");
        walletLabel.setFont(walletLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(walletLabel, "alignx left");

        walletCombo = StyledComponents.createStyledComboBox(48, 30);
        walletCombo.setPreferredSize(new Dimension(452, 48));
        walletCombo.setMinimumSize(new Dimension(452, 48));
        walletCombo.setMaximumSize(new Dimension(452, 48));
        walletCombo.setRenderer(new WalletListCellRenderer());
        DefaultComboBoxModel<WalletType> walletModel = new DefaultComboBoxModel<>();
        for (WalletType wt : walletTypes) {
            walletModel.addElement(wt);
        }
        walletCombo.setModel(walletModel);
        form.add(walletCombo, "alignx center, wrap");

        JLabel noteLabel = new JLabel("Note");
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.PLAIN, 14f));
        form.add(noteLabel, "alignx left");

        noteF = StyledComponents.createStyledTextField(452, 48, 30);
        form.add(noteF, "alignx center, wrap");

        JPanel buttonPanel = new JPanel(new MigLayout("ins 0, gap 10", "[220!][220!]", "[50!]"));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = StyledComponents.createSecondaryFunctionButton("Cancel", 220);
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);

        JButton saveBtn = StyledComponents.createPrimaryFunctionButton("Save", 220);
        saveBtn.addActionListener(e -> onSave());
        buttonPanel.add(saveBtn);

        form.add(buttonPanel, "w 450!, alignx center, wrap");

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        expenseBtn.setSelected(true);
        updateAmountColor();
        refillCategories();
    }

    private JPanel createTypeTogglePanel() {
        JPanel panel = new JPanel(new MigLayout("ins 0, gap 10", "[220!][220!]", "[50!]"));
        panel.setOpaque(false);

        typeGroup = new ButtonGroup();

        expenseBtn = StyledComponents.createStyledToggleButton("Expense", StyledComponents.ButtonType.DANGER,
                StyledComponents.ButtonSize.FUNCTION, 220);
        incomeBtn = StyledComponents.createStyledToggleButton("Income", StyledComponents.ButtonType.SUCCESS,
                StyledComponents.ButtonSize.FUNCTION, 220);

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

        panel.add(expenseBtn);
        panel.add(incomeBtn);

        return panel;
    }

    private void updateAmountColor() {
        if (expenseBtn.isSelected()) {
            amountF.setForeground(EXPENSE_COLOR);
        } else if (incomeBtn.isSelected()) {
            amountF.setForeground(INCOME_COLOR);
        }
    }

    private void refillCategories() {
        categoryGridPanel.removeAll();
        categoryPanels.clear();
        selectedCategoryPanel = null;

        String type = expenseBtn.isSelected() ? "expense" : "income";

        List<Category> list = listener.getCategoriesByType(type);

        String[] categoryOrder;
        if (type.equals("expense")) {
            categoryOrder = new String[] { "Food", "Transport", "Shopping", "Entertainment", "Bills", "Healthcare",
                    "Housing", "Education", "Other" };
        } else {
            categoryOrder = new String[] { "Salary", "Freelance", "Affiliate", "Selling", "Other Income" };
        }

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

        int rows = (int) Math.ceil(validCategoryCount / 3.0);
        if (rows == 0)
            rows = 1;
        StringBuilder rowConstraints = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            if (i > 0)
                rowConstraints.append(" ");
            rowConstraints.append("[75!]");
        }
        categoryGridPanel
                .setLayout(new MigLayout("ins 0, gap 10 10", "[140!][140!][140!]", rowConstraints.toString()));

        int index = 0;
        for (String catName : categoryOrder) {
            Category category = list.stream()
                    .filter(c -> c.getName().equals(catName))
                    .findFirst()
                    .orElse(null);

            if (category != null) {
                String iconPath = category.getIconPath();

                CategoryItemPanel panel = new CategoryItemPanel(category.getId(), category.getName(), iconPath);
                panel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (selectedCategoryPanel != null) {
                            selectedCategoryPanel.setSelected(false);
                        }
                        panel.setSelected(true);
                        selectedCategoryPanel = panel;
                    }
                });

                categoryPanels.put(category.getId(), panel);
                categoryGridPanel.add(panel, "cell " + (index % 3) + " " + (index / 3));
                index++;
            }
        }

        categoryGridPanel.revalidate();
        categoryGridPanel.repaint();
    }

    private void onSave() {
        String amountS = CurrencyUtil.parseRawNumber(amountF.getText());
        if (amountS.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount is required.");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(amountS);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount.");
            return;
        }
        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be positive.");
            return;
        }

        String type = expenseBtn.isSelected() ? "expense" : "income";

        if (selectedCategoryPanel == null) {
            JOptionPane.showMessageDialog(this, "Select a category.");
            return;
        }
        String categoryId = selectedCategoryPanel.getCategoryId();

        Date selectedDate = datePicker.getDate();
        Date selectedTime = timePicker.getTime();

        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Select a date.");
            return;
        }
        if (selectedTime == null) {
            JOptionPane.showMessageDialog(this, "Select a time.");
            return;
        }

        LocalDate d = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime t = selectedTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        String wallet = ((WalletType) walletCombo.getSelectedItem()).getName();
        String note = noteF.getText();
        if (note != null && note.length() > 120)
            note = note.substring(0, 120);

        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setType(type);
        tx.setCategoryId(categoryId);
        tx.setWalletType(wallet);
        tx.setNote(note.isEmpty() ? null : note);
        tx.setTransactionDate(d);
        tx.setTransactionTime(t);
        tx.setMonthKey(MonthKeyUtil.of(d));

        try {
            listener.onTransactionCreated(tx);
            listener.onRefreshRequired();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }
}
