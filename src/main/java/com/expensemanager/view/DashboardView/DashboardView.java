package com.expensemanager.view.DashboardView;

import com.expensemanager.controller.DashboardController;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.WalletType;
import com.expensemanager.service.CategoryService;
import com.expensemanager.service.TransactionService;
import com.expensemanager.service.WalletTypeService;
import com.expensemanager.view.CommonComponents.MainFrame;
import com.expensemanager.view.CommonComponents.StyledComponents;
import com.expensemanager.view.TransactionView.CreateTransactionDialog;
import com.expensemanager.view.TransactionView.TransactionDialogListener;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardView extends JPanel implements TransactionDialogListener {
    private static final Color MAIN_BG = Color.WHITE;
    private static final Color SUBTITLE_GRAY = new Color(0x6B7280);
    private static final Color CARD_BORDER = new Color(229, 231, 235);
    private static final int CARD_GAP = 20;
    private static final int HEADER_HEIGHT = 70;
    private static final int DATE_STRIP_HEIGHT = 70;
    private static final int CARD_ARC = 30;

    private final MainFrame main;
    private final DashboardController controller;
    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final WalletTypeService walletTypeService;

    public DashboardView(MainFrame main) {
        this.main = main;
        this.controller = new DashboardController(this);
        this.categoryService = new CategoryService();
        this.transactionService = new TransactionService();
        this.walletTypeService = new WalletTypeService();
        setBackground(MAIN_BG);
        setOpaque(true);
        setLayout(new MigLayout("ins 0, wrap 1, gap " + CARD_GAP + " " + CARD_GAP, "[grow,fill]",
                "[] [][][grow,fill] []"));
        addHierarchyListener(e -> {
            Component parent = getParent();
            while (parent != null) {
                if (parent instanceof JViewport) {
                    ((JViewport) parent).setBackground(MAIN_BG);
                    ((JViewport) parent).setOpaque(true);
                } else if (parent instanceof JScrollPane) {
                    ((JScrollPane) parent).getViewport().setBackground(MAIN_BG);
                    ((JScrollPane) parent).getViewport().setOpaque(true);
                }
                parent = parent.getParent();
            }
        });

        // (Header)
        JPanel header = buildHeaderPanel();
        add(header, "growx, h " + HEADER_HEIGHT + ", wrap");

        // (Date Selector)
        add(buildDateSelectorStrip(), "growx, wrap");

        // (KPI Cards)
        add(controller.getKpiCardsPanel(), "growx, wrap");

        // (Charts)
        JPanel chartsPanel = controller.getChartsPanel();
        chartsPanel.setMinimumSize(new Dimension(0, 600));
        add(chartsPanel, "growx, growy, pushy, wrap");

        // Budget Warning
        add(controller.getBudgetWarningsPanel(), "growx");
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new MigLayout("ins 14 20 14 20, gap 0", "[grow,fill][]", "[center]"));
        header.setBackground(Color.WHITE);

        JPanel left = new JPanel(new MigLayout("ins 0, gap 0, flowy", "[left]", "[]2[]"));
        left.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        left.add(title);
        JLabel subtitle = new JLabel("Overview of your finances");
        subtitle.setFont(subtitle.getFont().deriveFont(14f));
        subtitle.setForeground(SUBTITLE_GRAY);
        left.add(subtitle);
        header.add(left, "aligny center");

        JButton addTx = StyledComponents.createTitleButton("Add Transaction", StyledComponents.createPlusIcon(), 180);
        addTx.addActionListener(e -> new CreateTransactionDialog(main, this).setVisible(true));
        header.add(addTx, "aligny center");
        return header;
    }

    // TransactionDialogListener implementation

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
        main.refreshDashboard();
        main.refreshTransactions();
        main.refreshBudget();
    }

    private JPanel buildDateSelectorStrip() {
        JPanel inner = controller.getMonthSelectorPanel();
        return new DateStripCard(inner);
    }

    private static final class DateStripCard extends JPanel {
        DateStripCard(JPanel content) {
            setLayout(new BorderLayout());
            setOpaque(false);
            putClientProperty("FlatLaf.style", "arc: " + CARD_ARC);
            add(content, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, DATE_STRIP_HEIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, CARD_ARC, CARD_ARC);
            g2.dispose();
        }
    }

    public void onShown() {
        refresh();
    }

    public void refresh() {
        controller.refresh();
    }

    MainFrame getMain() {
        return main;
    }
}
