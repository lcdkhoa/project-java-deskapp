package com.expensemanager.controller;

import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.WalletType;
import com.expensemanager.service.CategoryService;
import com.expensemanager.service.TransactionService;
import com.expensemanager.service.WalletTypeService;
import com.expensemanager.util.ColorUtil;
import com.expensemanager.util.DateUtil;
import com.expensemanager.view.TransactionView.CreateTransactionDialog;
import com.expensemanager.view.TransactionView.TransactionDialogListener;
import com.expensemanager.view.TransactionView.TransactionRowPanel;
import com.expensemanager.view.TransactionView.TransactionView;

import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class TransactionController implements TransactionDialogListener {

    private static final Color BG_PAGE = Color.WHITE;

    private final TransactionView view;
    private final JPanel listPanel;
    private final JScrollPane scrollPane;

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final WalletTypeService walletTypeService;

    private final DateTimeFormatter filterDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public TransactionController(TransactionView view) {
        this.view = view;
        this.transactionService = new TransactionService();
        this.categoryService = new CategoryService();
        this.walletTypeService = new WalletTypeService();

        this.listPanel = new JPanel(
                new MigLayout("wrap 1, fillx, insets 0 0 16 0, gapy 12", "[grow,fill]", "[]"));
        this.listPanel.setOpaque(true);
        this.listPanel.setBackground(BG_PAGE);

        this.scrollPane = new JScrollPane(listPanel);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.scrollPane.setBackground(BG_PAGE);
        this.scrollPane.setOpaque(false);
        this.scrollPane.getViewport().setOpaque(true);
        this.scrollPane.getViewport().setBackground(BG_PAGE);
        this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void openAddTransaction() {
        new CreateTransactionDialog(view.getMain(), this).setVisible(true);
    }

    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @Override
    public List<Category> getCategoriesByType(String type) {
        return categoryService.getCategoriesByType(type);
    }

    @Override
    public List<WalletType> getWalletTypes() {
        return walletTypeService.getAllWalletTypes();
    }

    @Override
    public void onTransactionCreated(Transaction transaction) throws Exception {
        transactionService.createTransaction(transaction);
    }

    @Override
    public void onRefreshRequired() {
        view.getMain().refreshDashboard();
        view.getMain().refreshTransactions();
        view.getMain().refreshBudget();
    }

    public void refresh() {
        listPanel.removeAll();

        String categoryId = view.getSelectedCategoryId();
        String walletType = view.getSelectedWalletType();

        LocalDate startDate = parseFilterDate(view.getFromDateText());
        LocalDate endDate = parseFilterDate(view.getToDateText());

        String sortKey = view.getSortKey();
        String searchNote = view.getSearchText();

        try {

            Map<String, Category> idToCat = categoryService.getCategoryMap();
            List<Transaction> list = transactionService.searchTransactions(
                    categoryId, walletType, startDate, endDate, sortKey, searchNote);

            if (list.isEmpty()) {
                JLabel empty = new JLabel("No transactions");
                empty.setForeground(new Color(0x6B7280));
                empty.setBorder(BorderFactory.createEmptyBorder(24, 12, 24, 12));
                listPanel.add(empty, "growx");
            } else {
                LocalDate lastDate = null;
                for (Transaction t : list) {
                    LocalDate d = t.getTransactionDate();
                    if (d != null && !d.equals(lastDate)) {
                        addDateHeader(d);
                        lastDate = d;
                    }
                    addTransactionRow(t, idToCat.get(t.getCategoryId()));
                }
            }
        } catch (TransactionService.ServiceException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage());
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private void addDateHeader(LocalDate date) {
        JLabel header = new JLabel(DateUtil.formatDateForGroup(date));
        header.setFont(header.getFont().deriveFont(java.awt.Font.BOLD, 13f));
        header.setForeground(new Color(0x6B7280));
        header.setBorder(BorderFactory.createEmptyBorder(20, 4, 6, 4));
        listPanel.add(header, "growx");
    }

    private void addTransactionRow(Transaction t, Category cat) {
        String note = t.getNote() != null && !t.getNote().isBlank() ? t.getNote() : "(No note)";
        String wallet = toWalletDisplay(t.getWalletType());
        long amount = t.getAmount();
        String time = t.getTransactionTime() != null ? t.getTransactionTime().toString().substring(0, 5) : "";

        Color iconBg = ColorUtil.parseColor(cat != null ? cat.getLegendChartColor() : null, ColorUtil.SECONDARY_GRAY);

        String iconPath = null;
        String categoryName = null;
        if (cat != null) {
            iconPath = cat.getIconPath();
            categoryName = cat.getName();
        }
        if (iconPath == null) {
            iconPath = TransactionRowPanel.DEFAULT_CATEGORY_ICON;
        }

        TransactionRowPanel row = new TransactionRowPanel(iconPath, iconBg, note, categoryName, wallet, amount, time);
        listPanel.add(row, "growx");
    }

    private String toWalletDisplay(String walletType) {
        return TransactionService.toWalletDisplay(walletType);
    }

    private LocalDate parseFilterDate(String text) {
        if (text == null || text.isBlank())
            return null;
        try {
            return LocalDate.parse(text.trim(), filterDateFormatter);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
