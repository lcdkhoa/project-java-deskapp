package com.expensemanager.controller;

import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.WalletType;
import com.expensemanager.service.CategoryService;
import com.expensemanager.service.DashboardService;
import com.expensemanager.service.TransactionService;
import com.expensemanager.service.WalletTypeService;
import com.expensemanager.util.ChartUtils;
import com.expensemanager.util.ColorUtil;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.MonthKeyUtil;
import com.expensemanager.view.BudgetView.BudgetWarningsPanel;
import com.expensemanager.view.DashboardView.DashboardView;
import com.expensemanager.view.DashboardView.KPICard;
import com.expensemanager.view.CommonComponents.MainFrame;
import com.expensemanager.view.CommonComponents.ModernCard;
import com.expensemanager.view.TransactionView.CreateTransactionDialog;
import com.expensemanager.view.TransactionView.TransactionDialogListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

public class DashboardController implements TransactionDialogListener {
    private final DashboardView view;
    private YearMonth currentMonth;
    private final JLabel monthLabel;
    private JPanel kpiPanel;
    private JPanel chartsPanel;
    private BudgetWarningsPanel budgetWarningsPanel;

    private final DashboardService dashboardService;
    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final WalletTypeService walletTypeService;

    public DashboardController(DashboardView view) {
        this.view = view;
        this.dashboardService = new DashboardService();
        this.categoryService = new CategoryService();
        this.transactionService = new TransactionService();
        this.walletTypeService = new WalletTypeService();
        this.currentMonth = YearMonth.now();
        this.monthLabel = new JLabel(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        monthLabel.setFont(monthLabel.getFont().deriveFont(16f));
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
        MainFrame main = view.getMain();
        main.refreshDashboard();
        main.refreshTransactions();
        main.refreshBudget();
    }

    public void openAddTransaction() {
        new CreateTransactionDialog(view.getMain(), this).setVisible(true);
    }

    private JLabel backToCurrentLink;

    public JPanel getMonthSelectorPanel() {
        JPanel p = new JPanel(new MigLayout("ins 0 25 0 25, fill", "[][grow, center][]", "[center][]"));
        p.setOpaque(false);
        p.setBackground(Color.WHITE);

        JButton prevBtn = new JButton();
        ImageIcon leftIcon = com.expensemanager.view.CommonComponents.StyledComponents
                .getIcon("src/main/java/com/expensemanager/img/dashboard/left.png", 16, 16);
        prevBtn.setIcon(leftIcon);
        prevBtn.setBorder(BorderFactory.createEmptyBorder());
        prevBtn.setBorderPainted(false);
        prevBtn.setContentAreaFilled(false);
        prevBtn.setOpaque(false);
        prevBtn.setFocusPainted(false);
        prevBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prevBtn.putClientProperty("JButton.buttonType", "roundRect");
        prevBtn.addActionListener(e -> {
            prevMonth();
            view.refresh();
        });
        p.add(prevBtn, "cell 0 0, alignx center, aligny center, gaptop 30");
        p.add(monthLabel, "cell 1 0, alignx center, aligny center, gaptop 24");

        JButton nextBtn = new JButton();
        ImageIcon rightIcon = com.expensemanager.view.CommonComponents.StyledComponents
                .getIcon("src/main/java/com/expensemanager/img/dashboard/right.png", 16, 16);
        nextBtn.setIcon(rightIcon);
        nextBtn.setBorder(BorderFactory.createEmptyBorder());
        nextBtn.setBorderPainted(false);
        nextBtn.setContentAreaFilled(false);
        nextBtn.setOpaque(false);
        nextBtn.setFocusPainted(false);
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextBtn.putClientProperty("JButton.buttonType", "roundRect");
        nextBtn.addActionListener(e -> {
            nextMonth();
            view.refresh();
        });
        p.add(nextBtn, "cell 2 0, alignx center, aligny center, gaptop 30");

        backToCurrentLink = new JLabel("Back to current month");
        backToCurrentLink.setFont(backToCurrentLink.getFont().deriveFont(12f));
        backToCurrentLink.setForeground(new Color(0x2563EB));
        backToCurrentLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToCurrentLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                backToCurrent();
                view.refresh();
            }
        });
        backToCurrentLink.setVisible(!isCurrentMonth());
        p.add(backToCurrentLink, "cell 1 1, center, gapbottom 10");

        return p;
    }

    private void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        monthLabel.setText(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        updateBackButton();
    }

    private void nextMonth() {
        if (currentMonth.plusMonths(1).isAfter(YearMonth.now()))
            return;
        currentMonth = currentMonth.plusMonths(1);
        monthLabel.setText(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        updateBackButton();
    }

    private void backToCurrent() {
        currentMonth = YearMonth.now();
        monthLabel.setText(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        updateBackButton();
    }

    private boolean isCurrentMonth() {
        return currentMonth.equals(YearMonth.now());
    }

    private void updateBackButton() {
        if (backToCurrentLink != null) {
            backToCurrentLink.setVisible(!isCurrentMonth());
        }
    }

    public JPanel getKpiCardsPanel() {
        if (kpiPanel == null)
            kpiPanel = buildKpiPanel();
        return kpiPanel;
    }

    private JPanel buildKpiPanel() {
        JPanel panel = new JPanel(
                new MigLayout("ins 0, gap 20 0", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        return panel;
    }

    public JPanel getChartsPanel() {
        if (chartsPanel == null)
            chartsPanel = buildChartsPanel();
        return chartsPanel;
    }

    private JPanel buildChartsPanel() {
        JPanel panel = new JPanel(
                new MigLayout("ins 0, gap 20 0", "[grow 30, fill][grow 20, fill][grow 50, fill]",
                        "[grow, fill]"));
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        return panel;
    }

    public JPanel getBudgetWarningsPanel() {
        if (budgetWarningsPanel == null) {
            budgetWarningsPanel = new BudgetWarningsPanel();
        }
        return budgetWarningsPanel;
    }

    public void refresh() {
        String monthKey = MonthKeyUtil.of(currentMonth);

        DashboardService.KPIData kpiData = dashboardService.getKPIData(monthKey);

        refreshKpi(kpiData.expense, kpiData.income, kpiData.remaining,
                kpiData.budgetUsedPct, kpiData.totalBudget);
        refreshCharts(monthKey);
        refreshBudgetWarnings(monthKey);
    }

    private static final Color RED = new Color(0xEF4444);
    private static final Color GREEN = new Color(0x10B981);
    private static final Color BLUE = new Color(0x3B82F6);
    private static final Color PURPLE = new Color(0x8B5CF6);

    private void refreshKpi(long expense, long income, long remaining, double budgetUsedPct, long totalBudget) {
        JPanel p = getKpiCardsPanel();
        p.removeAll();
        p.add(new KPICard("Monthly Expense", CurrencyUtil.formatNoSymbol(Math.abs(expense)),
                "src/main/java/com/expensemanager/img/dashboard/down.png",
                RED), "grow");
        p.add(new KPICard("Monthly Income", CurrencyUtil.formatNoSymbol(income),
                "src/main/java/com/expensemanager/img/dashboard/up.png", GREEN),
                "grow");
        boolean remainingNeg = remaining < 0;
        p.add(new KPICard("Remaining", CurrencyUtil.formatNoSymbol(remaining),
                "src/main/java/com/expensemanager/img/dashboard/remains.png",
                remainingNeg ? RED : BLUE), "grow");
        if (!Double.isNaN(budgetUsedPct)) {
            p.add(new KPICard("Budget Used", String.format("%.0f%%", budgetUsedPct),
                    "src/main/java/com/expensemanager/img/dashboard/used.png", PURPLE),
                    "grow");
        } else {
            p.add(new KPICard("Budget Used", "-", "src/main/java/com/expensemanager/img/dashboard/used.png",
                    Color.GRAY), "grow");
        }
        p.revalidate();
        p.repaint();
    }

    private void refreshCharts(String monthKey) {
        JPanel p = getChartsPanel();
        p.removeAll();

        LocalDate referenceDate;
        YearMonth now = YearMonth.now();
        if (currentMonth.equals(now)) {
            referenceDate = LocalDate.now();
        } else {
            referenceDate = currentMonth.atEndOfMonth();
        }

        DashboardService.BarChartData barData = dashboardService.getLast7DaysExpense(referenceDate);

        DefaultCategoryDataset barSet = new DefaultCategoryDataset();
        Map<String, LocalDate> dateLabelToDate = new HashMap<>();

        for (int i = 0; i < barData.dates.size(); i++) {
            LocalDate d = barData.dates.get(i);
            long value = barData.amounts.get(i);
            String dateLabel = d.getDayOfMonth() + "/" + d.getMonthValue();
            dateLabelToDate.put(dateLabel, d);
            barSet.addValue(value, "Expense", dateLabel);
        }

        JPanel barCard = new ModernCard();
        if (!barData.hasData) {
            JLabel noDataLabel = new JLabel("No expense data", SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.PLAIN, 14f));
            noDataLabel.setForeground(new Color(0x6B7280));
            barCard.setLayout(new BorderLayout());
            barCard.add(noDataLabel, BorderLayout.CENTER);
        } else {
            JFreeChart bar = ChartFactory.createBarChart("Last 7 Days Spending", null, "Amount", barSet);
            ChartUtils.applyChartTitle(bar, "Last 7 Days Spending");
            bar.removeLegend();
            ChartUtils.applyBarChart(bar);

            org.jfree.chart.plot.CategoryPlot barPlot = bar.getCategoryPlot();
            barPlot.getRenderer().setDefaultToolTipGenerator(new Last7DaysToolTipGenerator(dateLabelToDate));

            ChartPanel barPanel = ChartUtils.createChartPanel(bar);
            barPanel.setDisplayToolTips(true);
            barCard.add(barPanel, BorderLayout.CENTER);
        }
        p.add(barCard, "grow");

        p.add(buildCategoryChartPanel(monthKey), "grow");

        p.add(buildMonthlyCashFlowPanel(monthKey), "grow");

        p.revalidate();
        p.repaint();
    }

    private JPanel buildCategoryChartPanel(String monthKey) {
        DashboardService.CategoryChartData chartData = dashboardService.getCategoryExpenseData(monthKey);

        List<Category> allCategories = chartData.categories;
        Map<String, Category> idToCategory = chartData.idToCategory;
        Map<String, Long> categoryExpenses = chartData.categoryExpenses;
        long totalExpense = chartData.totalExpense;

        DefaultPieDataset<String> pieSet = new DefaultPieDataset<>();
        for (Category cat : allCategories) {
            long expense = categoryExpenses.getOrDefault(cat.getId(), 0L);
            if (expense > 0) {
                pieSet.setValue(cat.getId(), expense);
            }
        }

        JPanel container = new JPanel(new MigLayout("fill, wrap 1", "[grow, fill]", "[][grow, fill][]")) {
            private static final int ARC = 30;
            private static final Color BORDER_COLOR = new Color(229, 231, 235);
            private static final int PADDING = 15;

            {
                setOpaque(false);
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
                putClientProperty("FlatLaf.style", "arc: " + ARC);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
                g2.dispose();
            }
        };

        JLabel titleLabel = ChartUtils.createChartTitleLabel("By Category");
        container.add(titleLabel, "growx, gapbottom 10");

        if (!chartData.hasData) {
            JLabel noDataLabel = new JLabel("No expense data", SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.PLAIN, 14f));
            noDataLabel.setForeground(new Color(0x6B7280));
            container.add(noDataLabel, "grow");
            return container;
        }

        JFreeChart chart = ChartFactory.createRingChart(null, pieSet, false, true, false);
        chart.setBackgroundPaint(null);

        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setSectionDepth(0.5);
        plot.setLabelGenerator(null);
        plot.setOutlineVisible(false);
        plot.setBackgroundPaint(null);
        plot.setShadowPaint(null);
        try {
            plot.setShadowGenerator(null);
        } catch (Exception ignored) {
        }
        plot.setSectionOutlinesVisible(true);
        plot.setSeparatorsVisible(false);

        BasicStroke whiteStroke = new BasicStroke(3f);
        for (Category cat : allCategories) {
            long expense = categoryExpenses.get(cat.getId());
            if (expense > 0) {
                Color catColor = ColorUtil.parseColor(cat.getLegendChartColor());
                plot.setSectionPaint(cat.getId(), catColor);
                plot.setSectionOutlinePaint(cat.getId(), Color.WHITE);
                plot.setSectionOutlineStroke(cat.getId(), whiteStroke);
            }
        }

        plot.setToolTipGenerator(new PieCategoryToolTipGenerator(idToCategory, categoryExpenses, totalExpense));

        ChartPanel chartPanel = ChartUtils.createChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(180, 180));

        container.add(chartPanel, "grow, align center");

        JPanel legendPanel = createCategoryLegendPanel(allCategories, categoryExpenses, idToCategory);
        container.add(legendPanel, "growx, gaptop 10");

        return container;
    }

    private JPanel createCategoryLegendPanel(List<Category> categories, Map<String, Long> expenses,
            Map<String, Category> idToCategory) {
        List<Category> categoriesWithExpense = new ArrayList<>();
        for (Category cat : categories) {
            long expense = expenses.getOrDefault(cat.getId(), 0L);
            if (expense > 0) {
                categoriesWithExpense.add(cat);
            }
        }

        JPanel legend = new JPanel(new MigLayout("ins 0, gap 20 0", "[grow 50, fill][grow 50, fill]", ""));
        legend.setOpaque(false);

        int total = categoriesWithExpense.size();
        int leftCount = (total + 1) / 2;

        JPanel leftColumn = new JPanel(new MigLayout("ins 0, gap 0 6, wrap 1", "[grow, fill]", ""));
        leftColumn.setOpaque(false);

        JPanel rightColumn = new JPanel(new MigLayout("ins 0, gap 0 6, wrap 1", "[grow, fill]", ""));
        rightColumn.setOpaque(false);

        for (int i = 0; i < categoriesWithExpense.size(); i++) {
            Category cat = categoriesWithExpense.get(i);
            long expense = expenses.getOrDefault(cat.getId(), 0L);
            Color catColor = ColorUtil.parseColor(cat.getLegendChartColor());
            String amountStr = CurrencyUtil.format(expense);

            JPanel itemPanel = new JPanel(new MigLayout("ins 0, fillx", "[]1[grow][]", "[center]"));
            itemPanel.setOpaque(false);

            JLabel dotLabel = new JLabel("●");
            dotLabel.setFont(dotLabel.getFont().deriveFont(20f));
            dotLabel.setForeground(catColor);
            itemPanel.add(dotLabel, "aligny center");

            JLabel nameLabel = new JLabel(cat.getName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 16f));
            nameLabel.setForeground(new Color(0x1F2937));
            itemPanel.add(nameLabel, "aligny center");

            JLabel amountLabel = new JLabel(amountStr);
            amountLabel.setFont(amountLabel.getFont().deriveFont(Font.PLAIN, 16f));
            amountLabel.setForeground(new Color(0x1F2937));
            itemPanel.add(amountLabel, "aligny center");

            if (i < leftCount) {
                leftColumn.add(itemPanel, "growx");
            } else {
                rightColumn.add(itemPanel, "growx");
            }
        }

        legend.add(leftColumn, "grow, aligny top");
        legend.add(rightColumn, "grow, aligny top");

        return legend;
    }

    private JPanel buildMonthlyCashFlowPanel(String monthKey) {
        DashboardService.MonthlyCashFlowData data = dashboardService.getMonthlyCashflow(monthKey, currentMonth);

        JPanel card = new ModernCard();
        card.setLayout(new BorderLayout());

        if (!data.hasData) {
            JLabel noDataLabel = new JLabel("No cash flow data", SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.PLAIN, 14f));
            noDataLabel.setForeground(new Color(0x6B7280));
            card.add(noDataLabel, BorderLayout.CENTER);
            return card;
        }

        XYSeries incomeSeries = new XYSeries("Green Line: Income");
        XYSeries expenseSeries = new XYSeries("Red Line: Expense");

        int daysInMonth = data.dayNumbers.size();
        for (int i = 0; i < daysInMonth; i++) {
            int day = data.dayNumbers.get(i);
            long income = data.incomeValues.get(i);
            long expense = data.expenseValues.get(i);
            incomeSeries.add(day, income);
            expenseSeries.add(day, expense);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(incomeSeries);
        dataset.addSeries(expenseSeries);

        String chartTitle = String.format("Monthly Cash Flow",
                data.month.getMonth().toString().substring(0, 1)
                        + data.month.getMonth().toString().substring(1).toLowerCase(),
                data.month.getYear());

        JFreeChart chart = ChartFactory.createXYLineChart(
                chartTitle,
                "Day of Month",
                "Amount (VND)",
                dataset);

        ChartUtils.applyChartTitle(chart, chartTitle);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(0xE5E7EB));
        plot.setRangeGridlinePaint(new Color(0xE5E7EB));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setOutlineVisible(false);

        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setRange(1, daysInMonth);
        xAxis.setTickUnit(new NumberTickUnit(1));
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        xAxis.setTickLabelPaint(new Color(0x6B7280));
        xAxis.setAxisLinePaint(new Color(0x9CA3AF));

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        yAxis.setTickLabelPaint(new Color(0x6B7280));
        yAxis.setAxisLinePaint(new Color(0x9CA3AF));
        yAxis.setNumberFormatOverride(new java.text.DecimalFormat("#,##0") {
            @Override
            public StringBuffer format(double number, StringBuffer result, java.text.FieldPosition fieldPosition) {
                if (number >= 1_000_000) {
                    return super.format(number / 1_000_000, result, fieldPosition).append("M");
                } else if (number >= 1_000) {
                    return super.format(number / 1_000, result, fieldPosition).append("K");
                }
                return super.format(number, result, fieldPosition);
            }
        });

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);

        BasicStroke smoothStroke = new BasicStroke(
                2.0f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);

        Color incomeGreen = new Color(0x22C55E);
        renderer.setSeriesPaint(0, incomeGreen);
        renderer.setSeriesStroke(0, smoothStroke);
        renderer.setSeriesShapesVisible(0, false);

        Color expenseRed = new Color(0xDC2626);
        renderer.setSeriesPaint(1, expenseRed);
        renderer.setSeriesStroke(1, smoothStroke);
        renderer.setSeriesShapesVisible(1, false);

        renderer.setDefaultToolTipGenerator(new MonthlyCashFlowToolTipGenerator(data));

        plot.setRenderer(renderer);

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setPosition(RectangleEdge.TOP);
            legend.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            legend.setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
            legend.setBackgroundPaint(Color.WHITE);
            legend.setPadding(new RectangleInsets(2, 10, 2, 10));
        }

        ChartPanel chartPanel = ChartUtils.createChartPanel(chart);
        chartPanel.setDisplayToolTips(true);
        card.add(chartPanel, BorderLayout.CENTER);

        return card;
    }

    private static class MonthlyCashFlowToolTipGenerator implements XYToolTipGenerator {
        private final DashboardService.MonthlyCashFlowData data;

        MonthlyCashFlowToolTipGenerator(DashboardService.MonthlyCashFlowData data) {
            this.data = data;
        }

        @Override
        public String generateToolTip(org.jfree.data.xy.XYDataset dataset, int series, int item) {
            int day = (int) dataset.getXValue(series, item);
            long value = (long) dataset.getYValue(series, item);

            LocalDate date = data.month.atDay(day);
            String dateStr = String.format("%02d/%02d/%d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
            String amountStr = CurrencyUtil.format(value);
            String type = series == 0 ? "Income" : "Expense";
            String color = series == 0 ? "#22C55E" : "#DC2626";

            return String.format(
                    "<html><div style='padding: 5px;'>" +
                            "<b>%s</b><br>" +
                            "<span style='color: %s; font-weight: bold;'>%s: %s</span>" +
                            "</div></html>",
                    dateStr, color, type, amountStr);
        }
    }

    private static class PieCategoryToolTipGenerator implements PieToolTipGenerator {
        private final Map<String, Category> idToCategory;
        private final Map<String, Long> expenses;
        private final long totalExpense;

        PieCategoryToolTipGenerator(Map<String, Category> idToCategory, Map<String, Long> expenses, long totalExpense) {
            this.idToCategory = idToCategory;
            this.expenses = expenses;
            this.totalExpense = totalExpense;
        }

        @Override
        @SuppressWarnings({ "rawtypes" })
        public String generateToolTip(org.jfree.data.general.PieDataset dataset, Comparable key) {
            Category cat = idToCategory.get(key.toString());
            if (cat == null)
                return "";

            long expense = expenses.getOrDefault(cat.getId(), 0L);
            String amountStr = CurrencyUtil.format(expense);
            double percentage = totalExpense > 0 ? (expense * 100.0 / totalExpense) : 0.0;
            String pctStr = String.format("%.1f%%", percentage);

            Color catColor = ColorUtil.parseColor(cat.getLegendChartColor());
            String colorHex = String.format("#%02x%02x%02x", catColor.getRed(), catColor.getGreen(),
                    catColor.getBlue());

            return String.format(
                    "<html><div style='padding: 5px;'>" +
                            "<span style='color: %s; font-weight: bold;'>● %s</span><br>" +
                            "%s<br>" +
                            "%s of total" +
                            "</div></html>",
                    colorHex, cat.getName(), amountStr, pctStr);
        }
    }

    private static class Last7DaysToolTipGenerator implements org.jfree.chart.labels.CategoryToolTipGenerator {
        private final Map<String, LocalDate> dateLabelToDate;

        Last7DaysToolTipGenerator(Map<String, LocalDate> dateLabelToDate) {
            this.dateLabelToDate = dateLabelToDate;
        }

        @Override
        public String generateToolTip(CategoryDataset dataset, int row, int column) {
            Comparable<?> categoryKey = dataset.getColumnKey(column);
            Number value = dataset.getValue(row, column);
            if (value == null)
                return "";

            String dateLabel = categoryKey.toString();
            LocalDate date = dateLabelToDate.get(dateLabel);
            String dateStr;

            if (date != null) {
                LocalDate today = LocalDate.now();
                if (date.equals(today)) {
                    dateStr = "Today";
                } else if (date.equals(today.minusDays(1))) {
                    dateStr = "Yesterday";
                } else {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("MMM-dd");
                    dateStr = date.format(formatter);
                }
            } else {
                dateStr = dateLabel;
            }

            String amountStr = CurrencyUtil.formatNoSymbol(value.longValue());

            return String.format(
                    "<html><center><span style='color:#3B82F6'>%s</span><br/><span style='font-size:14px; font-weight:normal; color:#3B82F6'>%s đ</span></center></html>",
                    dateStr, amountStr);
        }
    }

    private void refreshBudgetWarnings(String monthKey) {
        DashboardService.BudgetWarningsData warningsData = dashboardService.getBudgetWarnings(monthKey);

        BudgetWarningsPanel card = (BudgetWarningsPanel) getBudgetWarningsPanel();
        card.setWarnings(warningsData.overBudgetItems, warningsData.categoryIdToName);
        card.setVisible(warningsData.hasWarnings());
        card.revalidate();
        card.repaint();
    }
}
