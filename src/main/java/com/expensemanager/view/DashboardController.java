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
import org.jfree.chart.JFreeChart;
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
        // 4 columns [grow,fill] equal width, gap 20
        return new JPanel(new MigLayout("ins 0, gap 20 0", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
    }

    public JPanel getChartsPanel() {
        if (chartsPanel == null) chartsPanel = buildChartsPanel();
        return chartsPanel;
    }

    private JPanel buildChartsPanel() {
        // weightx 0.35, 0.3, 0.35. Row [grow,fill]. gap 20.
        return new JPanel(new MigLayout("ins 0, gap 20", "[grow 35][grow 30][grow 35]", "[grow,fill]"));
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

    // Section 1.2: Expense #EF4444, Income #10B981, Remaining #3B82F6, Budget Used purple
    private static final Color RED = new Color(0xEF4444);
    private static final Color GREEN = new Color(0x10B981);
    private static final Color BLUE = new Color(0x3B82F6);
    private static final Color PURPLE = new Color(0x8B5CF6);

    private void refreshKpi(long expense, long income, long remaining, double budgetUsedPct, long totalBudget) {
        JPanel p = getKpiCardsPanel();
        p.removeAll();
        p.add(new KPICard("Monthly Expense", CurrencyUtil.format(Math.abs(expense)), "↓", RED, RED), "grow");
        p.add(new KPICard("Monthly Income", CurrencyUtil.format(income), "↑", GREEN, GREEN), "grow");
        boolean remainingNeg = remaining < 0;
        p.add(new KPICard("Remaining", CurrencyUtil.format(remaining), "◆", remainingNeg ? RED : BLUE, remainingNeg ? RED : BLUE), "grow");
        if (!Double.isNaN(budgetUsedPct)) {
            p.add(new KPICard("Budget Used", String.format("%.0f%%", budgetUsedPct), "%", PURPLE, PURPLE), "grow");
        } else {
            p.add(new KPICard("Budget Used", "-", "%", Color.GRAY, Color.GRAY), "grow");
        }
        p.revalidate();
        p.repaint();
    }

    private void refreshCharts(Connection conn, String userId, String monthKey, TransactionDAO txDao, BudgetDAO bDao, CategoryDAO cDao) throws SQLException {
        JPanel p = getChartsPanel();
        p.removeAll();

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
        ChartUtils.applyBarChart(bar);
        p.add(new ModernCard(ChartUtils.createChartPanel(bar)), "grow");

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
        JFreeChart pie = ChartFactory.createRingChart("By Category", pieSet, true, true, false);
        ChartUtils.applyDonutChart(pie, pieSet);
        p.add(new ModernCard(ChartUtils.createChartPanel(pie)), "grow");

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
        ChartUtils.applyLineChart(line);
        p.add(new ModernCard(ChartUtils.createChartPanel(line)), "grow");

        p.revalidate();
        p.repaint();
    }

    private void refreshBudgetWarnings(Connection conn, String userId, String monthKey, BudgetDAO bDao, CategoryDAO cDao) throws SQLException {
        List<BudgetDAO.BudgetUsedRow> rows = bDao.getBudgetUsedPerCategory(conn, userId, monthKey);
        List<BudgetDAO.BudgetUsedRow> over = rows.stream().filter(r -> r.percentUsed >= 100).sorted((a,b)->Double.compare(b.percentUsed,a.percentUsed)).collect(Collectors.toList());

        Map<String, String> idToName = new HashMap<>();
        for (Category c : cDao.findAll(conn)) idToName.put(c.getId(), c.getName());

        BudgetWarningsPanel card = (BudgetWarningsPanel) getBudgetWarningsPanel();
        card.setWarnings(over, idToName);
        card.setVisible(!over.isEmpty());
        card.revalidate();
        card.repaint();
    }
}
