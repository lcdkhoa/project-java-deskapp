package com.expensemanager.view.TransactionView;

import com.expensemanager.controller.TransactionController;
import com.expensemanager.model.Category;
import com.expensemanager.model.WalletType;
import com.expensemanager.view.CommonComponents.CalendarPicker;
import com.expensemanager.view.CommonComponents.MainFrame;
import com.expensemanager.view.CommonComponents.SortListCellRenderer;
import com.expensemanager.view.CommonComponents.SortListCellRenderer.SortItem;
import com.expensemanager.view.CommonComponents.StyledComponents;
import com.expensemanager.view.CommonComponents.CategoryListCellRenderer;
import com.expensemanager.view.CommonComponents.WalletListCellRenderer;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

public class TransactionView extends JPanel {
    private static final Color BG_PAGE = Color.WHITE;
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final int CARD_ARC = 30;
    private static final int CONTROL_HEIGHT = 48;

    private static final Color WALLET_COLOR = new Color(0x6B7280);

    private final MainFrame main;
    private final TransactionController controller;

    private JTextField searchField;
    private JComboBox<Object> categoryCombo;
    private JComboBox<Object> walletCombo;
    private CalendarPicker fromDateField;
    private CalendarPicker toDateField;
    private JComboBox<SortItem> sortCombo;

    public TransactionView(MainFrame main) {
        this.main = main;
        this.controller = new TransactionController(this);
        setLayout(new MigLayout("wrap 1, fill, insets 20", "[grow]", "[][][][grow]"));
        setBackground(BG_PAGE);

        add(createHeaderPanel(), "growx");
        add(createSearchRow(), "growx, gapright 12");
        add(createFilterCard(), "growx, gapright 12");

        JScrollPane scrollPane = controller.getScrollPane();
        add(scrollPane, "grow, push");

        refresh();
    }

    private JComponent createHeaderPanel() {
        JPanel header = new JPanel(new MigLayout("fillx, insets 0", "[grow][right]", "[]"));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new MigLayout("ins 0, wrap 2", "[]", "[]2[]"));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Transactions");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titlePanel.add(titleLabel, "wrap");

        JLabel subtitleLabel = new JLabel("Your transaction history");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 14f));
        subtitleLabel.setForeground(WALLET_COLOR);
        titlePanel.add(subtitleLabel);

        header.add(titlePanel, "growx");

        JButton addTx = StyledComponents.createTitleButton("Add Transaction", StyledComponents.createPlusIcon(), 180);
        addTx.addActionListener(e -> controller.openAddTransaction());
        header.add(addTx, "right");

        return header;
    }

    private JComponent createSearchRow() {
        JPanel row = new JPanel(new MigLayout("fillx, insets 10 0 10 2", "[grow][]", "[]"));
        row.setOpaque(false);
        row.add(createSearchField(), "growx");

        JButton resetButton = StyledComponents.createSecondaryFunctionButton("Reset", 80);
        resetButton.addActionListener(e -> resetFilters());
        row.add(resetButton);

        return row;
    }

    private void resetFilters() {
        if (searchField != null) {
            searchField.setText("");
        }
        if (categoryCombo != null && categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
        if (walletCombo != null && walletCombo.getItemCount() > 0) {
            walletCombo.setSelectedIndex(0);
        }
        if (sortCombo != null && sortCombo.getItemCount() > 0) {
            sortCombo.setSelectedIndex(0);
        }
        if (fromDateField != null) {
            fromDateField.clear();
        }
        if (toDateField != null) {
            toDateField.clear();
        }
        refresh();
    }

    private JComponent createFilterCard() {
        JPanel card = new JPanel(new MigLayout("fillx, insets 16, wrap 3, gapx 18, gapy 14",
                "[grow,fill][grow,fill][grow,fill]", "[]"));
        card.setOpaque(false);

        JPanel outer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CARD_ARC, CARD_ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        outer.setOpaque(false);
        outer.add(card, BorderLayout.CENTER);

        card.add(createLabeledInput("Category", createCategoryCombo()), "growx");
        card.add(createLabeledInput("Wallet", createWalletCombo()), "growx");
        card.add(createLabeledInput("Sort by", createSortCombo()), "growx");

        JLabel dr = new JLabel("Date Range (Max 60 days)");
        dr.setFont(dr.getFont().deriveFont(Font.PLAIN, 12f));
        dr.setForeground(WALLET_COLOR);
        card.add(dr, "span 3, gaptop 2");

        JPanel datesRow = new JPanel(new MigLayout("ins 0, fillx, gapx 12", "[grow,fill][grow,fill]", "[]"));
        datesRow.setOpaque(false);

        fromDateField = new CalendarPicker("mm/dd/yyyy");
        toDateField = new CalendarPicker("mm/dd/yyyy");
        fromDateField.clear();
        toDateField.clear();
        fromDateField.addChangeListener(() -> controller.refresh(TransactionController.FilterSource.FROM_DATE));
        toDateField.addChangeListener(() -> controller.refresh(TransactionController.FilterSource.TO_DATE));

        datesRow.add(createLabeledInput("From", fromDateField), "growx");
        datesRow.add(createLabeledInput("To", toDateField), "growx");

        card.add(datesRow, "span 3, growx");

        return outer;
    }

    private JComponent createLabeledInput(String label, JComponent input) {
        JPanel p = new JPanel(new MigLayout("ins 0, wrap 1, fillx, gapy 6", "[grow]", "[]"));
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        l.setForeground(new Color(0x374151));

        p.add(l, "growx");
        p.add(input, "growx");
        return p;
    }

    private JComponent createSearchField() {
        final int searchHeight = 50;

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CARD_ARC, CARD_ARC);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(0, searchHeight));
        wrapper.setMinimumSize(new Dimension(0, searchHeight));

        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x6B7280));

                int w = 16;
                int h = 16;
                int x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;

                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x, y, 10, 10);
                g2.drawLine(x + 8, y + 8, x + 14, y + 14);

                g2.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(48, searchHeight));

        searchField = new JTextField();
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        searchField.setOpaque(false);
        searchField.putClientProperty("JTextField.placeholderText", "Search transactions...");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });

        wrapper.add(iconLabel, BorderLayout.WEST);
        wrapper.add(searchField, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent createCategoryCombo() {
        categoryCombo = createStyledComboBox();
        categoryCombo.setRenderer(new CategoryListCellRenderer());
        categoryCombo.addItem("All categories");
        List<Category> all = controller.getAllCategories();
        for (Category c : all) {
            categoryCombo.addItem(c);
        }
        categoryCombo.addActionListener(e -> refresh());
        return categoryCombo;
    }

    private JComponent createWalletCombo() {
        walletCombo = createStyledComboBox();
        walletCombo.setRenderer(new WalletListCellRenderer());
        walletCombo.addItem("All wallets");
        List<WalletType> wallets = controller.getWalletTypes();
        for (WalletType wt : wallets) {
            walletCombo.addItem(wt);
        }
        walletCombo.addActionListener(e -> refresh());
        return walletCombo;
    }

    private JComponent createSortCombo() {
        sortCombo = createStyledComboBox();
        sortCombo.setRenderer(new SortListCellRenderer());

        sortCombo.addItem(new SortItem("date_desc", "Date (Newest first)",
                "src/main/java/com/expensemanager/img/menu/lastest.png"));
        sortCombo.addItem(new SortItem("date_asc", "Date (Oldest first)",
                "src/main/java/com/expensemanager/img/menu/oldest.png"));
        sortCombo.addItem(new SortItem("amount_desc", "Amount (Highest first)",
                "src/main/java/com/expensemanager/img/menu/amount_most.png"));
        sortCombo.addItem(new SortItem("amount_asc", "Amount (Lowest first)",
                "src/main/java/com/expensemanager/img/menu/amount_less.png"));

        sortCombo.addActionListener(e -> refresh());
        return sortCombo;
    }

    private <T> JComboBox<T> createStyledComboBox() {
        return StyledComponents.createStyledComboBox(CONTROL_HEIGHT, CARD_ARC);
    }

    public void refresh() {
        controller.refresh();
    }

    public void onShown() {
        refresh();
    }

    public MainFrame getMain() {
        return main;
    }

    public String getSearchText() {
        return searchField != null ? searchField.getText() : null;
    }

    public String getSelectedCategoryId() {
        Object selected = categoryCombo != null ? categoryCombo.getSelectedItem() : null;
        if (selected instanceof Category) {
            return ((Category) selected).getId();
        }
        return null;
    }

    public String getSelectedWalletType() {
        Object selected = walletCombo != null ? walletCombo.getSelectedItem() : null;
        if (selected instanceof WalletType) {
            return ((WalletType) selected).getName();
        }
        return null;
    }

    public String getFromDateText() {
        return fromDateField != null ? fromDateField.getText() : null;
    }

    public void resetFromDate() {
        if (fromDateField != null) {
            SwingUtilities.invokeLater(() -> fromDateField.clear());
        }
    }

    public String getToDateText() {
        return toDateField != null ? toDateField.getText() : null;
    }

    public void resetToDate() {
        if (toDateField != null) {
            SwingUtilities.invokeLater(() -> toDateField.clear());
        }
    }

    public String getSortKey() {
        if (sortCombo != null && sortCombo.getSelectedItem() instanceof SortItem) {
            return ((SortItem) sortCombo.getSelectedItem()).getKey();
        }
        return "date_desc";
    }

}
