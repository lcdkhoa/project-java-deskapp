package com.expensemanager.view.TransactionView;

import com.expensemanager.controller.TransactionController;
import com.expensemanager.model.Category;
import com.expensemanager.view.CommonComponents.CalendarPicker;
import com.expensemanager.view.CommonComponents.MainFrame;
import com.expensemanager.view.CommonComponents.StyledComponents;

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
    private JComboBox<CategoryItem> categoryCombo;
    private JComboBox<WalletItem> walletCombo;
    private CalendarPicker fromDateField;
    private CalendarPicker toDateField;
    private JComboBox<SortItem> sortCombo;

    public TransactionView(MainFrame main) {
        this.main = main;
        this.controller = new TransactionController(this);
        setLayout(new MigLayout("wrap 1, fill, insets 20", "[grow]", "[][][][grow]"));
        setBackground(BG_PAGE);

        add(createHeaderPanel(), "growx");
        add(createSearchRow(), "growx");
        add(createFilterCard(), "growx");

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
        JPanel row = new JPanel(new MigLayout("fillx, insets 10 0 10 0", "[grow]", "[]"));
        row.setOpaque(false);
        row.add(createSearchField(), "growx");
        return row;
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
        fromDateField.addChangeListener(this::refresh);
        toDateField.addChangeListener(this::refresh);

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

        categoryCombo.addActionListener(e -> refresh());
        reloadCategories();

        return categoryCombo;
    }

    private void reloadCategories() {
        categoryCombo.removeAllItems();
        categoryCombo.addItem(new CategoryItem(null, "All categories"));

        List<Category> all = controller.getAllCategories();
        for (Category c : all) {
            categoryCombo.addItem(new CategoryItem(c.getId(), c.getName()));
        }
    }

    private JComponent createWalletCombo() {
        walletCombo = createStyledComboBox();
        walletCombo.addItem(new WalletItem(null, "All wallets"));
        walletCombo.addItem(new WalletItem("cash", "Cash"));
        walletCombo.addItem(new WalletItem("bank_transfer", "Bank Transfer"));
        walletCombo.addItem(new WalletItem("card", "Card"));
        walletCombo.addItem(new WalletItem("e_wallet", "E-wallet"));
        walletCombo.addActionListener(e -> refresh());
        return walletCombo;
    }

    private JComponent createSortCombo() {
        sortCombo = createStyledComboBox();
        sortCombo.addItem(new SortItem("date_desc", "Date (Newest first)"));
        sortCombo.addItem(new SortItem("date_asc", "Date (Oldest first)"));
        sortCombo.addItem(new SortItem("amount_desc", "Amount (Highest first)"));
        sortCombo.addItem(new SortItem("amount_asc", "Amount (Lowest first)"));
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
        CategoryItem categoryItem = (CategoryItem) (categoryCombo != null ? categoryCombo.getSelectedItem() : null);
        return categoryItem != null ? categoryItem.id : null;
    }

    public String getSelectedWalletType() {
        WalletItem walletItem = (WalletItem) (walletCombo != null ? walletCombo.getSelectedItem() : null);
        return walletItem != null ? walletItem.value : null;
    }

    public String getFromDateText() {
        return fromDateField != null ? fromDateField.getText() : null;
    }

    public String getToDateText() {
        return toDateField != null ? toDateField.getText() : null;
    }

    public String getSortKey() {
        if (sortCombo != null && sortCombo.getSelectedItem() instanceof SortItem) {
<<<<<<< HEAD:src/main/java/com/expensemanager/view/TransactionView.java
            sortKey = ((SortItem) sortCombo.getSelectedItem()).key;
        }

        String searchNote = searchField != null ? searchField.getText() : null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            TransactionDAO txDao = new TransactionDAO();
            CategoryDAO cDao = new CategoryDAO();
            Map<String, Category> idToCat = new HashMap<>();
            for (Category c : cDao.findAll(conn)) {
                idToCat.put(c.getId(), c);
            }

            List<Transaction> list = txDao.search(conn, userId, categoryId, walletType, startDate, endDate, sortKey,
                    searchNote);
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
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + ex.getMessage());
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private void addDateHeader(LocalDate date) {
        JLabel header = new JLabel(DateUtil.formatDateForGroup(date));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
        header.setForeground(new Color(0x6B7280));
        header.setBorder(BorderFactory.createEmptyBorder(20, 4, 6, 4));
        listPanel.add(header, "growx");
    }

    private void addTransactionRow(Transaction t, Category cat) {
        String note = t.getNote() != null && !t.getNote().isBlank() ? t.getNote() : "(No note)";
        String wallet = toWalletDisplay(t.getWalletType());
        long amount = t.getAmount();
        String time = t.getTransactionTime() != null ? t.getTransactionTime().toString().substring(0, 5) : "";

        Color iconBg = parseColor(cat != null ? cat.getLegendChartColor() : null);

        // Use icon_path from DB, fallback to static mapping by name
        String iconPath = null;
        if (cat != null) {
            iconPath = cat.getIconPath();
        }
        if (iconPath == null) {
            iconPath = DEFAULT_CATEGORY_ICON;
        }
        TransactionRowItem row = new TransactionRowItem(iconBg, iconPath, note, wallet, amount, time);
        listPanel.add(row, "growx");
    }

    private String toWalletDisplay(String walletType) {
        if (walletType == null || walletType.isBlank())
            return "";
        switch (walletType) {
            case "cash":
                return "Cash";
            case "bank_transfer":
                return "Bank Transfer";
            case "card":
                return "Card";
            case "e_wallet":
                return "E-wallet";
            default:
                return walletType;
        }
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

    private static Color parseColor(String hex) {
        if (hex == null || hex.isBlank())
            return new Color(0x9CA3AF);
        if (!hex.startsWith("#"))
            hex = "#" + hex;
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return new Color(0x9CA3AF);
        }
    }

    /**
     * Card-style transaction row using MigLayout as specified.
     * MigLayout("fillx, insets 10 20 10 20", "[44!]15[grow][right]", "center")
     */
    private static class TransactionRowItem extends JPanel {
        private static final int ROW_ARC = 22;

        TransactionRowItem(Color iconBgColor, String iconPath, String note, String wallet,
                long amount, String timeHhmm) {
            super(new MigLayout("fillx, insets 10 20 10 20", "[44!]15[grow,fill][right]", "center"));
            setOpaque(false);

            // Custom paint: white rounded card with subtle border
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            CircleIconPanel icon = new CircleIconPanel(iconBgColor, iconPath);
            add(icon, "cell 0 0");

            JPanel center = new JPanel(new MigLayout("ins 0, fillx, wrap 2", "[grow]", "[]8[]"));
            center.setOpaque(false);

            JLabel noteLbl = new JLabel(note);
            noteLbl.setFont(noteLbl.getFont().deriveFont(Font.BOLD, 14f));
            noteLbl.setForeground(NOTE_COLOR);
            center.add(noteLbl, "growx, span");

            JLabel walletLbl = new JLabel(wallet != null ? wallet : "");
            walletLbl.setFont(walletLbl.getFont().deriveFont(Font.PLAIN, 12f));
            walletLbl.setForeground(WALLET_COLOR);
            center.add(walletLbl, "growx, span");

            add(center, "cell 1 0, growx");

            JPanel right = new JPanel(new MigLayout("ins 0, wrap 1", "[right]", "[]4[]"));
            right.setOpaque(false);

            JLabel amountLbl = new JLabel(CurrencyUtil.formatSigned(amount));
            amountLbl.setFont(amountLbl.getFont().deriveFont(Font.BOLD, 14f));
            amountLbl.setForeground(amount < 0 ? EXPENSE_COLOR : INCOME_COLOR);
            right.add(amountLbl, "right");

            JLabel timeLbl = new JLabel(timeHhmm != null ? timeHhmm : "");
            timeLbl.setFont(timeLbl.getFont().deriveFont(Font.PLAIN, 12f));
            timeLbl.setForeground(WALLET_COLOR);
            right.add(timeLbl, "right");

            add(right, "cell 2 0, alignx right");
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ROW_ARC, ROW_ARC);

            // Subtle card border (matches screenshot better than full shadow).
            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ROW_ARC, ROW_ARC);
            g2.dispose();
            super.paintComponent(g);
=======
            return ((SortItem) sortCombo.getSelectedItem()).key;
>>>>>>> 8f07de0 (refactoring controller):src/main/java/com/expensemanager/view/TransactionView/TransactionView.java
        }
        return "date_desc";
    }

    private static class CategoryItem {
        final String id;
        final String label;

        CategoryItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class WalletItem {
        final String value;
        final String label;

        WalletItem(String value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class SortItem {
        final String key;
        final String label;

        SortItem(String key, String label) {
            this.key = key;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
