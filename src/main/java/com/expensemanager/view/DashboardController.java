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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
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

    private void refreshKpi(long expense, long income, long remaining, double budgetUsedPct, long totalBudget) {
        JPanel p = getKpiCardsPanel();
        p.removeAll();
        p.setLayout(new GridLayout(1, 4, 12, 0));
        p.add(kpiCard("Monthly Expense", "↓ " + CurrencyUtil.format(Math.abs(expense)), new Color(0xB91C1C)));
        p.add(kpiCard("Monthly Income", "↑ " + CurrencyUtil.format(income), new Color(0x16A34A)));
        p.add(kpiCard("Remaining", CurrencyUtil.format(remaining), remaining < 0 ? new Color(0xB91C1C) : new Color(0x16A34A)));
        if (!Double.isNaN(budgetUsedPct)) {
            p.add(kpiCard("Budget Used", String.format("%.0f%%", budgetUsedPct), new Color(0x4F46E5)));
        } else {
            p.add(kpiCard("Budget Used", "-", Color.GRAY));
        }
        p.revalidate();
        p.repaint();
    }

    private JPanel kpiCard(String title, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout(8, 8));
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(accent, 2), BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        c.add(new JLabel(title), BorderLayout.NORTH);
        JLabel v = new JLabel(value);
        v.setFont(v.getFont().deriveFont(18f));
        v.setForeground(accent);
        c.add(v, BorderLayout.CENTER);
        return c;
    }

    private void refreshCharts(Connection conn, String userId, String monthKey, TransactionDAO txDao, BudgetDAO bDao, CategoryDAO cDao) throws SQLException {
        JPanel p = getChartsPanel();
        p.removeAll();
        p.setLayout(new GridLayout(1, 3, 12, 12));

        // Last 7 Days
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
        CategoryPlot barPlot = bar.getCategoryPlot();
        barPlot.getDomainAxis().setVisible(false);
        ((BarRenderer)barPlot.getRenderer()).setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
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
        ((PiePlot)pie.getPlot()).setCircular(true);
        ((PiePlot)pie.getPlot()).setInteriorGap(0.35); // donut
        p.add(new ChartPanel(pie, 280, 180, 80, 80, 1024, 768, true, true, true, true, true, true));

        // Monthly Cashflow (Line)
        Map<LocalDate, Long> cf = txDao.getCashflowByDay(conn, userId, monthKey);
        XYSeries series = new XYSeries("Cashflow");
        int days = currentMonth.lengthOfMonth();
        for (int i = 1; i <= days; i++) {
            LocalDate d = currentMonth.atDay(i);
            series.add(i, cf.getOrDefault(d, 0L));
        }
        JFreeChart line = ChartFactory.createXYLineChart("Monthly Cashflow", "Day", "Amount", new XYSeriesCollection(series));
        line.removeLegend();
        XYPlot xyPlot = line.getXYPlot();
        xyPlot.getDomainAxis().setVisible(false);
        xyPlot.setBackgroundPaint(null);
        XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) xyPlot.getRenderer();
        r.setSeriesShapesVisible(0, true);
        r.setSeriesLinesVisible(0, true);
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
