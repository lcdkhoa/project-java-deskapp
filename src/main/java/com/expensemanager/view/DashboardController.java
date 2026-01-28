package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;
import com.expensemanager.util.ChartUtils;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.MonthKeyUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Dashboard. Section 1. KPIs, charts, budget warnings from DB.
 */
public class DashboardController {
    private final DashboardView view;
    private YearMonth currentMonth;
    private final JLabel monthLabel;
    private JPanel kpiPanel;
    private JPanel chartsPanel;
    private BudgetWarningsPanel budgetWarningsPanel;

    public DashboardController(DashboardView view) {
        this.view = view;
        this.currentMonth = YearMonth.now();
        this.monthLabel = new JLabel(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        monthLabel.setFont(monthLabel.getFont().deriveFont(16f));
    }

    private JLabel backToCurrentLink;

    public JPanel getMonthSelectorPanel() {
        JPanel p = new JPanel(new MigLayout("ins 0 25 0 25, fill", "[][grow, center][]", "[center][]"));
        p.setOpaque(false);
        p.setBackground(Color.WHITE);

        // Previous button with left.png icon - centered vertically
        JButton prevBtn = new JButton();
        ImageIcon leftIcon = com.expensemanager.util.UIUtils.getIcon("src/main/java/com/expensemanager/img/dashboard/left.png", 24, 24);
        if (leftIcon != null) {
            prevBtn.setIcon(leftIcon);
        } else {
            prevBtn.setText("<");
        }
        prevBtn.setBorderPainted(false);
        prevBtn.setContentAreaFilled(false);
        prevBtn.setOpaque(false);
        prevBtn.addActionListener(e -> {
            prevMonth();
            view.refresh();
        });
        p.add(prevBtn, "cell 0 0, aligny center");

        // Month label - centered both horizontally and vertically
        p.add(monthLabel, "cell 1 0, alignx center, aligny center");

        // Next button with right.png icon - centered vertically
        JButton nextBtn = new JButton();
        ImageIcon rightIcon = com.expensemanager.util.UIUtils.getIcon("src/main/java/com/expensemanager/img/dashboard/right.png", 24, 24);
        if (rightIcon != null) {
            nextBtn.setIcon(rightIcon);
        } else {
            nextBtn.setText(">");
        }
        nextBtn.setBorderPainted(false);
        nextBtn.setContentAreaFilled(false);
        nextBtn.setOpaque(false);
        nextBtn.addActionListener(e -> {
            nextMonth();
            view.refresh();
        });
        p.add(nextBtn, "cell 2 0, aligny center");

        // Back to current month link - centered below the date label
        backToCurrentLink = new JLabel("Back to current month");
        backToCurrentLink.setFont(backToCurrentLink.getFont().deriveFont(12f));
        backToCurrentLink.setForeground(new Color(0x2563EB)); // Blue color
        backToCurrentLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToCurrentLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                backToCurrent();
                view.refresh();
            }
        });
        backToCurrentLink.setVisible(!isCurrentMonth());
        p.add(backToCurrentLink, "cell 1 1, center");

        return p;
    }

    private void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        monthLabel.setText(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        updateBackButton();
    }

    private void nextMonth() {
        if (currentMonth.plusMonths(1).isAfter(YearMonth.now()))
            return; // no future
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
        // 4 columns [grow,fill] equal width, gap 20
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
        // weightx 0.35, 0.3, 0.35. Row [grow,fill]. gap 20.
        JPanel panel = new JPanel(new MigLayout("ins 0, gap 20", "[grow 35][grow 30][grow 35]", "[grow,fill]"));
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

    void refresh() {
        String userId = AppContext.getUserId();
        String monthKey = MonthKeyUtil.of(currentMonth);
        try (Connection conn = DatabaseConnection.getConnection()) {
            TransactionDAO txDao = new TransactionDAO();
            BudgetDAO bDao = new BudgetDAO();
            CategoryDAO cDao = new CategoryDAO();

            long expense = txDao.getMonthlyExpense(conn, userId, monthKey);
            long income = txDao.getMonthlyIncome(conn, userId, monthKey);
            long remaining = income - Math.abs(expense);
            long totalBudget = bDao.getTotalBudget(conn, userId, monthKey);
            double budgetUsedPct = totalBudget > 0 ? (Math.abs(expense) * 100.0 / totalBudget) : Double.NaN;

            refreshKpi(expense, income, remaining, budgetUsedPct, totalBudget);
            refreshCharts(conn, userId, monthKey, txDao, bDao, cDao);
            refreshBudgetWarnings(conn, userId, monthKey, bDao, cDao);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Error loading dashboard: " + ex.getMessage());
        }
    }

    // Section 1.2: Expense #EF4444, Income #10B981, Remaining #3B82F6, Budget Used
    // purple
    private static final Color RED = new Color(0xEF4444);
    private static final Color GREEN = new Color(0x10B981);
    private static final Color BLUE = new Color(0x3B82F6);
    private static final Color PURPLE = new Color(0x8B5CF6);

    private void refreshKpi(long expense, long income, long remaining, double budgetUsedPct, long totalBudget) {
        JPanel p = getKpiCardsPanel();
        p.removeAll();
        p.add(new KPICard("Monthly Expense", CurrencyUtil.formatNoSymbol(Math.abs(expense)), "src/main/java/com/expensemanager/img/dashboard/down.png",
                RED), "grow");
        p.add(new KPICard("Monthly Income", CurrencyUtil.formatNoSymbol(income), "src/main/java/com/expensemanager/img/dashboard/up.png", GREEN),
                "grow");
        boolean remainingNeg = remaining < 0;
        p.add(new KPICard("Remaining", CurrencyUtil.formatNoSymbol(remaining), "src/main/java/com/expensemanager/img/dashboard/remains.png",
                remainingNeg ? RED : BLUE), "grow");
        if (!Double.isNaN(budgetUsedPct)) {
            p.add(new KPICard("Budget Used", String.format("%.0f%%", budgetUsedPct), "src/main/java/com/expensemanager/img/dashboard/used.png", PURPLE),
                    "grow");
        } else {
            p.add(new KPICard("Budget Used", "-", "src/main/java/com/expensemanager/img/dashboard/used.png", Color.GRAY), "grow");
        }
        p.revalidate();
        p.repaint();
    }

    private void refreshCharts(Connection conn, String userId, String monthKey, TransactionDAO txDao, BudgetDAO bDao,
            CategoryDAO cDao) throws SQLException {
        JPanel p = getChartsPanel();
        p.removeAll();

        // Last 7 Days (Section 1.3: no axes, bar bo tròn, soft blue)
        // Data Range: Based on selectedDate (currentMonth)
        // If currentMonth == Current Month: Show last 7 days leading up to Today
        // If currentMonth != Current Month: Show last 7 days of that month
        LocalDate referenceDate;
        YearMonth now = YearMonth.now();
        if (currentMonth.equals(now)) {
            // Current month: show last 7 days up to today
            referenceDate = LocalDate.now();
        } else {
            // Past/Future month: show last 7 days of that month
            referenceDate = currentMonth.atEndOfMonth();
        }
        LocalDate start = referenceDate.minusDays(6);
        LocalDate end = referenceDate;
        Map<LocalDate, Long> byDate = txDao.getExpenseByDateRange(conn, userId, start, end);
        DefaultCategoryDataset barSet = new DefaultCategoryDataset();
        boolean hasData = false;
        // Store date mapping for tooltip
        Map<String, LocalDate> dateLabelToDate = new HashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            long value = byDate.getOrDefault(d, 0L);
            if (value > 0)
                hasData = true;
            String dateLabel = d.getDayOfMonth() + "/" + d.getMonthValue();
            dateLabelToDate.put(dateLabel, d);
            barSet.addValue(value, "Expense", dateLabel);
        }

        JPanel barCard = new ModernCard();
        if (!hasData) {
            // Show "No expense data" message
            JLabel noDataLabel = new JLabel("No expense data", SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.PLAIN, 14f));
            noDataLabel.setForeground(new Color(0x6B7280));
            barCard.setLayout(new BorderLayout());
            barCard.add(noDataLabel, BorderLayout.CENTER);
        } else {
            JFreeChart bar = ChartFactory.createBarChart("Last 7 Days Spending", null, "Amount", barSet);
            applyChartTitleStyle(bar, "Last 7 Days Spending");
            bar.removeLegend();
            ChartUtils.applyBarChart(bar);

            // Set custom tooltip generator for bar chart with date mapping
            org.jfree.chart.plot.CategoryPlot barPlot = bar.getCategoryPlot();
            barPlot.getRenderer().setDefaultToolTipGenerator(new Last7DaysToolTipGenerator(dateLabelToDate));

            ChartPanel barPanel = ChartUtils.createChartPanel(bar);
            barPanel.setDisplayToolTips(true);
            barCard.add(barPanel, BorderLayout.CENTER);
        }
        p.add(barCard, "grow");

        // By Category (Donut) - new design with custom legend and HTML tooltip
        p.add(buildCategoryChartPanel(conn, userId, monthKey, txDao, cDao), "grow");

        // Monthly Cashflow (Line): smooth spline, no dots, teal, no axes/grid (Section
        // 1.5)
        // Divide values by 1000 to reduce chart height
        Map<LocalDate, Long> cf = txDao.getCashflowByDay(conn, userId, monthKey);
        XYSeries series = new XYSeries("Cashflow");
        boolean hasCashflowData = false;
        int days = currentMonth.lengthOfMonth();
        for (int i = 1; i <= days; i++) {
            LocalDate d = currentMonth.atDay(i);
            long value = cf.getOrDefault(d, 0L);
            if (value != 0)
                hasCashflowData = true;
            // Divide by 1000 to reduce chart height (1 unit = 1000)
            series.add(i, value / 1000.0);
        }

        JPanel lineCard = new ModernCard();
        if (!hasCashflowData) {
            // Show "No expense data" message
            JLabel noDataLabel = new JLabel("No expense data", SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.PLAIN, 14f));
            noDataLabel.setForeground(new Color(0x6B7280));
            lineCard.setLayout(new BorderLayout());
            lineCard.add(noDataLabel, BorderLayout.CENTER);
        } else {
            JFreeChart line = ChartFactory.createXYLineChart("Monthly Cashflow", "Day", "Amount",
                    new XYSeriesCollection(series));
            applyChartTitleStyle(line, "Monthly Cashflow");
            line.removeLegend();
            ChartUtils.applyLineChart(line);

            // Set custom tooltip generator for line chart
            XYPlot linePlot = (XYPlot) line.getPlot();
            linePlot.getRenderer().setDefaultToolTipGenerator(new MonthlyCashflowToolTipGenerator(currentMonth));

            // Increase stroke width to make line thicker
            if (linePlot.getRenderer() instanceof org.jfree.chart.renderer.xy.XYSplineRenderer) {
                org.jfree.chart.renderer.xy.XYSplineRenderer renderer = (org.jfree.chart.renderer.xy.XYSplineRenderer) linePlot
                        .getRenderer();
                renderer.setSeriesStroke(0, new BasicStroke(3.0f)); // Thicker line (was default ~1.0f)
            }

            ChartPanel linePanel = ChartUtils.createChartPanel(line);
            linePanel.setDisplayToolTips(true);
            lineCard.add(linePanel, BorderLayout.CENTER);
        }
        p.add(lineCard, "grow");

        p.revalidate();
        p.repaint();
    }

    /**
     * Build By Category chart panel with split layout: chart on left, custom legend
     * on right.
     * Shows all categories (including 0 values in legend), only > 0 in chart.
     */
    private JPanel buildCategoryChartPanel(Connection conn, String userId, String monthKey,
            TransactionDAO txDao, CategoryDAO cDao) throws SQLException {
        // Fetch all expense categories
        List<Category> allCategories = cDao.findByType(conn, "expense");

        // Fetch expense data by category
        Map<String, Long> expenseByCategory = txDao.getExpenseByCategory(conn, userId, monthKey);

        // Create maps for lookup
        Map<String, Category> idToCategory = new HashMap<>();
        Map<String, Long> categoryExpenses = new HashMap<>();
        for (Category cat : allCategories) {
            idToCategory.put(cat.getId(), cat);
            categoryExpenses.put(cat.getId(), expenseByCategory.getOrDefault(cat.getId(), 0L));
        }

        // Calculate total for percentage
        long totalExpense = categoryExpenses.values().stream().mapToLong(Long::longValue).sum();

        // Build dataset: only add categories with value > 0
        DefaultPieDataset<String> pieSet = new DefaultPieDataset<>();
        boolean hasCategoryData = false;
        for (Category cat : allCategories) {
            long expense = categoryExpenses.get(cat.getId());
            if (expense > 0) {
                pieSet.setValue(cat.getId(), expense);
                hasCategoryData = true;
            }
        }

        // Create container panel with same padding as ModernCard (20px)
        JPanel container = new JPanel(new MigLayout("wrap 1, fill", "[center]", "[grow][min]")) {
            private static final int ARC = 30;
            private static final Color BORDER_COLOR = new Color(229, 231, 235); // #E5E7EB
            private static final int PADDING = 20; // Same as ModernCard

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

        if (!hasCategoryData) {
            // Show "No expense data" message
            JLabel noDataLabel = new JLabel("No expense data", SwingConstants.CENTER);
            noDataLabel.setFont(noDataLabel.getFont().deriveFont(Font.PLAIN, 14f));
            noDataLabel.setForeground(new Color(0x6B7280));
            container.add(noDataLabel, "cell 0 0, grow");
            return container;
        }

        // Create ring chart
        JFreeChart chart = ChartFactory.createRingChart("By Category", pieSet, false, true, false);
        applyChartTitleStyle(chart, "By Category");
        chart.removeLegend(); // Remove default legend

        // Configure RingPlot
        RingPlot plot = (RingPlot) chart.getPlot();
        plot.setSectionDepth(0.5); // Thick donut ring
        plot.setLabelGenerator(null); // Hide connecting labels
        plot.setOutlineVisible(false); // No outer border
        plot.setBackgroundPaint(null); // Transparent
        plot.setShadowPaint(null); // Disable shadow to remove dirty look
        try {
            plot.setShadowGenerator(null);
        } catch (Exception ignored) {
        }
        // White gaps between slices: thick white outline
        plot.setSectionOutlinesVisible(true);

        // Set colors and white outlines from Category.legend_chart_color
        // Only set for categories that are in the dataset (expense > 0)
        // Use thinner white stroke for narrow white gaps (no gray lines)
        BasicStroke whiteStroke = new BasicStroke(2.0f); // Narrow white gap
        for (Category cat : allCategories) {
            long expense = categoryExpenses.get(cat.getId());
            if (expense > 0) {
                Color catColor = parseColor(cat.getLegendChartColor());
                plot.setSectionPaint(cat.getId(), catColor);
                // Set white outline for each section to create narrow white gaps (no gray)
                plot.setSectionOutlinePaint(cat.getId(), Color.WHITE);
                plot.setSectionOutlineStroke(cat.getId(), whiteStroke);
            }
        }

        // Set HTML tooltip generator
        plot.setToolTipGenerator(new PieCategoryToolTipGenerator(idToCategory, categoryExpenses, totalExpense));

        // Create chart panel
        ChartPanel chartPanel = ChartUtils.createChartPanel(chart);

        // Create custom legend panel
        JPanel legendPanel = createCategoryLegendPanel(allCategories, categoryExpenses, idToCategory);

        // Cell 1 (Top): The ChartPanel (The Donut)
        container.add(chartPanel, "cell 0 0, grow");
        // Cell 2 (Bottom): The CustomLegendPanel
        container.add(legendPanel, "cell 0 1, grow");

        return container;
    }

    /**
     * Create custom legend panel showing only categories with expense > 0, with
     * colored dots, names,
     * and amounts. Layout: GridLayout with 2 columns.
     */
    private JPanel createCategoryLegendPanel(List<Category> categories, Map<String, Long> expenses,
            Map<String, Category> idToCategory) {
        // Filter categories with expense > 0
        List<Category> categoriesWithExpense = new ArrayList<>();
        for (Category cat : categories) {
            long expense = expenses.getOrDefault(cat.getId(), 0L);
            if (expense > 0) {
                categoriesWithExpense.add(cat);
            }
        }

        // Use GridLayout(0, 2, 10, 10) for 2 columns with 10px gaps
        JPanel legend = new JPanel(new GridLayout(0, 2, 10, 10));
        legend.setOpaque(false);
        legend.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (Category cat : categoriesWithExpense) {
            long expense = expenses.getOrDefault(cat.getId(), 0L);
            Color catColor = parseColor(cat.getLegendChartColor());
            String amountStr = CurrencyUtil.format(expense);

            // Legend item panel: [Color Dot] [Category Name] ....... [Amount]
            JPanel itemPanel = new JPanel(new MigLayout("ins 0, fillx", "[20][]push[]", "[center]"));
            itemPanel.setOpaque(false);

            // Colored dot (circle)
            JLabel dotLabel = new JLabel("●");
            dotLabel.setFont(dotLabel.getFont().deriveFont(16f));
            dotLabel.setForeground(catColor);
            itemPanel.add(dotLabel, "cell 0 0, alignx left, aligny center");

            // Category name - 16px font
            JLabel nameLabel = new JLabel(cat.getName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 16f));
            itemPanel.add(nameLabel, "cell 1 0, alignx left, aligny center");

            // Amount - plain font (not bold), 16px font
            JLabel amountLabel = new JLabel(amountStr);
            amountLabel.setFont(amountLabel.getFont().deriveFont(Font.PLAIN, 16f));
            itemPanel.add(amountLabel, "cell 2 0, alignx right, aligny center");

            legend.add(itemPanel);
        }

        return legend;
    }

    /**
     * Parse hex color string to Color object.
     */
    private static Color parseColor(String hex) {
        if (hex == null || hex.isBlank())
            return new Color(0x6B7280); // Default gray
        if (!hex.startsWith("#"))
            hex = "#" + hex;
        try {
            return Color.decode(hex);
        } catch (Exception e) {
            return new Color(0x6B7280); // Default gray on error
        }
    }

    /**
     * Custom HTML tooltip generator for category chart.
     */
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
        public String generateToolTip(org.jfree.data.general.PieDataset dataset, Comparable key) {
            Category cat = idToCategory.get(key.toString());
            if (cat == null)
                return "";

            long expense = expenses.getOrDefault(cat.getId(), 0L);
            String amountStr = CurrencyUtil.format(expense);
            double percentage = totalExpense > 0 ? (expense * 100.0 / totalExpense) : 0.0;
            String pctStr = String.format("%.1f%%", percentage);

            Color catColor = parseColor(cat.getLegendChartColor());
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

    /**
     * Custom tooltip generator for Last 7 Days Spending bar chart.
     * Format: HTML with Date on line 1 (Today/Yesterday/mmm-dd), Amount on line 2
     * (plain, 14px).
     */
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
                    // Format: mmm-dd (e.g., "Dec-17", "Jan-26")
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

    /**
     * Custom tooltip generator for Monthly Cashflow line chart.
     * Format: HTML with Date on line 1, Cashflow Amount on line 2 (bold, 14px).
     */
    private static class MonthlyCashflowToolTipGenerator implements XYToolTipGenerator {
        private final YearMonth month;

        MonthlyCashflowToolTipGenerator(YearMonth month) {
            this.month = month;
        }

        @Override
        public String generateToolTip(org.jfree.data.xy.XYDataset dataset, int series, int item) {
            Number xValue = dataset.getX(series, item);
            Number yValue = dataset.getY(series, item);
            if (xValue == null || yValue == null)
                return "";

            int dayOfMonth = xValue.intValue();
            LocalDate date = month.atDay(dayOfMonth);
            String dateStr = String.format("%02d/%02d", date.getDayOfMonth(), date.getMonthValue());
            // Multiply by 1000 since values were divided by 1000 for display
            long actualValue = (long) (yValue.doubleValue() * 1000);
            String amountStr = CurrencyUtil.formatNoSymbol(actualValue);

            return String.format(
                    "<html><center>%s<br/><span style='font-size:14px; font-weight:normal'>%s đ</span></center></html>",
                    dateStr, amountStr);
        }
    }

    /**
     * Apply standardized chart title styling: center aligned, 24px PLAIN font,
     * padding.
     */
    private void applyChartTitleStyle(JFreeChart chart, String title) {
        TextTitle textTitle = new TextTitle(title);
        textTitle.setFont(textTitle.getFont().deriveFont(Font.PLAIN, 24f));
        textTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        textTitle.setPadding(new RectangleInsets(20, 0, 20, 0));
        chart.setTitle(textTitle);
    }

    private void refreshBudgetWarnings(Connection conn, String userId, String monthKey, BudgetDAO bDao,
            CategoryDAO cDao) throws SQLException {
        List<BudgetDAO.BudgetUsedRow> rows = bDao.getBudgetUsedPerCategory(conn, userId, monthKey);
        List<BudgetDAO.BudgetUsedRow> over = rows.stream().filter(r -> r.percentUsed >= 100)
                .sorted((a, b) -> Double.compare(b.percentUsed, a.percentUsed)).collect(Collectors.toList());

        Map<String, String> idToName = new HashMap<>();
        for (Category c : cDao.findAll(conn))
            idToName.put(c.getId(), c.getName());

        BudgetWarningsPanel card = (BudgetWarningsPanel) getBudgetWarningsPanel();
        card.setWarnings(over, idToName);
        card.setVisible(!over.isEmpty());
        card.revalidate();
        card.repaint();
    }
}
