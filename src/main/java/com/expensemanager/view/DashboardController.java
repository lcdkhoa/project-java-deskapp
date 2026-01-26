package com.expensemanager.view;

import com.expensemanager.AppContext;
import com.expensemanager.dao.BudgetDAO;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.db.DatabaseConnection;
import com.expensemanager.model.Category;
import com.expensemanager.util.CurrencyUtil;
import com.expensemanager.util.MonthKeyUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
    private JPanel budgetWarningsPanel;

    public DashboardController(DashboardView view) {
        this.view = view;
        this.currentMonth = YearMonth.now();
        this.monthLabel = new JLabel(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        monthLabel.setFont(monthLabel.getFont().deriveFont(16f));
    }

    public JPanel getMonthSelectorPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        p.add(new JButton("<") {{
            addActionListener(e -> { prevMonth(); view.refresh(); });
        }});
        p.add(monthLabel);
        p.add(new JButton(">") {{
            addActionListener(e -> { nextMonth(); view.refresh(); });
        }});
        JButton back = new JButton("Back to current month");
        back.addActionListener(e -> { backToCurrent(); view.refresh(); });
        back.setVisible(!isCurrentMonth());
        p.add(back);
        return p;
    }

    private void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        monthLabel.setText(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        updateBackButton();
    }

    private void nextMonth() {
        if (currentMonth.plusMonths(1).isAfter(YearMonth.now())) return; // no future
        currentMonth = currentMonth.plusMonths(1);
        monthLabel.setText(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        updateBackButton();
    }

    private void backToCurrent() {
        currentMonth = YearMonth.now();
        monthLabel.setText(MonthKeyUtil.toLabel(MonthKeyUtil.of(currentMonth)));
        updateBackButton();
    }

    private boolean isCurrentMonth() { return currentMonth.equals(YearMonth.now()); }

    private void updateBackButton() {
        for (Component c : ((Container)monthLabel.getParent()).getComponents()) {
            if (c instanceof JButton && ((JButton)c).getText().equals("Back to current month")) {
                ((JButton)c).setVisible(!isCurrentMonth());
                break;
            }
        }
    }

    public JPanel getKpiCardsPanel() {
        if (kpiPanel == null) kpiPanel = buildKpiPanel();
        return kpiPanel;
    }

    private JPanel buildKpiPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0));
        // Will be filled in refresh()
        return p;
    }

    public JPanel getChartsPanel() {
        if (chartsPanel == null) chartsPanel = buildChartsPanel();
        return chartsPanel;
    }

    private JPanel buildChartsPanel() {
        JPanel p = new JPanel(new GridLayout(1, 3, 12, 12));
        // Placeholders; filled in refresh()
        return p;
    }

    public JPanel getBudgetWarningsPanel() {
        if (budgetWarningsPanel == null) budgetWarningsPanel = new JPanel(new BorderLayout());
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

    // Section 1.2: Expense red, Income green, Remaining red/green by value, Budget Used indigo or gray
    private static final Color RED = new Color(0xB91C1C);
    private static final Color GREEN = new Color(0x16A34A);
    private static final Color INDIGO = new Color(0x4F46E5);

    // Section 6.5.2: Expense category colors for donut
    private static final Map<String, Color> CATEGORY_COLORS = new HashMap<>();
    static {
        CATEGORY_COLORS.put("Food", new Color(0x4F46E5));
        CATEGORY_COLORS.put("Transport", new Color(0x6366F1));
        CATEGORY_COLORS.put("Housing", new Color(0x8B5CF6));
        CATEGORY_COLORS.put("Bills", new Color(0x7C3AED));
        CATEGORY_COLORS.put("Shopping", new Color(0xEC4899));
        CATEGORY_COLORS.put("Entertainment", new Color(0xF59E0B));
        CATEGORY_COLORS.put("Coffee", new Color(0xA16207));
        CATEGORY_COLORS.put("Healthcare", new Color(0x22C55E));
        CATEGORY_COLORS.put("Education", new Color(0x0EA5E9));
        CATEGORY_COLORS.put("Other", new Color(0x6B7280));
    }

    private static final Color SOFT_BLUE = new Color(0x93C5FD);
    private static final Color TEAL = new Color(0x14B8A6);

    private void refreshKpi(long expense, long income, long remaining, double budgetUsedPct, long totalBudget) {
        JPanel p = getKpiCardsPanel();
        p.removeAll();
        p.setLayout(new GridLayout(1, 4, 12, 0));
        p.add(new KPICard("Monthly Expense", CurrencyUtil.format(Math.abs(expense)), "↓", RED, RED));
        p.add(new KPICard("Monthly Income", CurrencyUtil.format(income), "↑", GREEN, GREEN));
        boolean remainingNeg = remaining < 0;
        p.add(new KPICard("Remaining", CurrencyUtil.format(remaining), "◆", remainingNeg ? RED : GREEN, remainingNeg ? RED : GREEN));
        if (!Double.isNaN(budgetUsedPct)) {
            p.add(new KPICard("Budget Used", String.format("%.0f%%", budgetUsedPct), "%", INDIGO, INDIGO));
        } else {
            p.add(new KPICard("Budget Used", "-", "%", Color.GRAY, Color.GRAY));
        }
        p.revalidate();
        p.repaint();
    }

    private void refreshCharts(Connection conn, String userId, String monthKey, TransactionDAO txDao, BudgetDAO bDao, CategoryDAO cDao) throws SQLException {
        JPanel p = getChartsPanel();
        p.removeAll();
        p.setLayout(new GridLayout(1, 3, 12, 12));

        // Last 7 Days (Section 1.3: no axes, bar bo tròn, soft blue)
        LocalDate lastDay = currentMonth.atEndOfMonth();
        LocalDate start = lastDay.minusDays(6);
        if (start.getMonthValue() != currentMonth.getMonthValue()) start = currentMonth.atDay(1);
        Map<LocalDate, Long> byDate = txDao.getExpenseByDateRange(conn, userId, start, lastDay);
        DefaultCategoryDataset barSet = new DefaultCategoryDataset();
        for (LocalDate d = start; !d.isAfter(lastDay); d = d.plusDays(1)) {
            barSet.addValue(byDate.getOrDefault(d, 0L), "Expense", d.getDayOfMonth() + "/" + currentMonth.getMonthValue());
        }
        JFreeChart bar = ChartFactory.createBarChart("Last 7 Days Spending", null, "Amount", barSet);
        bar.removeLegend();
        bar.setBackgroundPaint(Color.WHITE);
        CategoryPlot barPlot = bar.getCategoryPlot();
        barPlot.setBackgroundPaint(Color.WHITE);
        barPlot.setOutlineVisible(false);
        barPlot.setDomainGridlinesVisible(false);
        barPlot.setRangeGridlinesVisible(false);
        barPlot.getDomainAxis().setVisible(false);
        barPlot.getRangeAxis().setVisible(false);
        BarRenderer barR = (BarRenderer) barPlot.getRenderer();
        barR.setBarPainter(new RoundedBarPainter());
        barR.setShadowVisible(false);
        barR.setSeriesPaint(0, SOFT_BLUE);
        p.add(new ChartPanel(bar, 280, 180, 80, 80, 1024, 768, true, true, true, true, true, true));

        // By Category (Donut) - max 6, rest in Other
        Map<String, Long> byCat = txDao.getExpenseByCategory(conn, userId, monthKey);
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(byCat.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        DefaultPieDataset pieSet = new DefaultPieDataset();
        Map<String, String> idToName = new HashMap<>();
        for (Category c : cDao.findAll(conn)) idToName.put(c.getId(), c.getName());
        int n = 0;
        long otherSum = 0;
        for (Map.Entry<String, Long> e : sorted) {
            if (n < 6) {
                pieSet.setValue(idToName.getOrDefault(e.getKey(), e.getKey()), e.getValue());
                n++;
            } else {
                otherSum += e.getValue();
            }
        }
        if (otherSum > 0) pieSet.setValue("Other", otherSum);
        JFreeChart pie = ChartFactory.createPieChart("By Category", pieSet, false, true, false);
        pie.setBackgroundPaint(Color.WHITE);
        PiePlot piePlot = (PiePlot) pie.getPlot();
        piePlot.setCircular(true);
        piePlot.setInteriorGap(0.40);
        piePlot.setBackgroundPaint(Color.WHITE);
        piePlot.setOutlineVisible(false);
        for (Object key : pieSet.getKeys()) {
            String name = key.toString();
            piePlot.setSectionPaint((Comparable<?>) key, CATEGORY_COLORS.getOrDefault(name, Color.GRAY));
        }
        p.add(new ChartPanel(pie, 280, 180, 80, 80, 1024, 768, true, true, true, true, true, true));

        // Monthly Cashflow (Line): smooth spline, no dots, teal, no axes/grid (Section 1.5)
        Map<LocalDate, Long> cf = txDao.getCashflowByDay(conn, userId, monthKey);
        XYSeries series = new XYSeries("Cashflow");
        int days = currentMonth.lengthOfMonth();
        for (int i = 1; i <= days; i++) {
            LocalDate d = currentMonth.atDay(i);
            series.add(i, cf.getOrDefault(d, 0L));
        }
        JFreeChart line = ChartFactory.createXYLineChart("Monthly Cashflow", "Day", "Amount", new XYSeriesCollection(series));
        line.removeLegend();
        line.setBackgroundPaint(Color.WHITE);
        XYPlot xyPlot = line.getXYPlot();
        xyPlot.setBackgroundPaint(Color.WHITE);
        xyPlot.setOutlineVisible(false);
        xyPlot.setDomainGridlinesVisible(false);
        xyPlot.setRangeGridlinesVisible(false);
        xyPlot.getDomainAxis().setVisible(false);
        xyPlot.getRangeAxis().setVisible(false);
        XYSplineRenderer splineR = new XYSplineRenderer();
        splineR.setSeriesPaint(0, TEAL);
        splineR.setSeriesShapesVisible(0, false);
        splineR.setSeriesLinesVisible(0, true);
        xyPlot.setRenderer(splineR);
        p.add(new ChartPanel(line, 280, 180, 80, 80, 1024, 768, true, true, true, true, true, true));

        p.revalidate();
        p.repaint();
    }

    private void refreshBudgetWarnings(Connection conn, String userId, String monthKey, BudgetDAO bDao, CategoryDAO cDao) throws SQLException {
        List<BudgetDAO.BudgetUsedRow> rows = bDao.getBudgetUsedPerCategory(conn, userId, monthKey);
        List<BudgetDAO.BudgetUsedRow> over = rows.stream().filter(r -> r.percentUsed >= 100).sorted((a,b)->Double.compare(b.percentUsed,a.percentUsed)).collect(Collectors.toList());
        JPanel pan = getBudgetWarningsPanel();
        pan.removeAll();
        if (over.isEmpty()) {
            pan.setVisible(false);
        } else {
            pan.setVisible(true);
            pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
            pan.add(new JLabel("Budget Warnings"));
            Map<String, String> idToName = new HashMap<>();
            for (Category c : cDao.findAll(conn)) idToName.put(c.getId(), c.getName());
            for (BudgetDAO.BudgetUsedRow r : over) {
                pan.add(new JLabel(idToName.getOrDefault(r.categoryId, r.categoryId) + " " + CurrencyUtil.format(r.spent) + " / " + CurrencyUtil.format(r.budget) + " (" + String.format("%.0f%%", r.percentUsed) + ")"));
            }
            pan.revalidate();
            pan.repaint();
        }
    }
}
