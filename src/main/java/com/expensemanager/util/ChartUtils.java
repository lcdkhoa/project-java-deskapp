package com.expensemanager.util;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;

import com.expensemanager.view.DashboardView.RoundedBarPainter;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class ChartUtils {

    public static final Color SOFT_BLUE = new Color(0x93C5FD);
    public static final Color TEAL = new Color(0x14B8A6);
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Color TICK_LABEL_GRAY = new Color(0x6B7280);
    private static final Font TICK_LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

    private static final String CHART_TITLE_FONT_FAMILY = "Segoe UI";
    private static final int CHART_TITLE_FONT_SIZE = 18;
    private static final int CHART_TITLE_PADDING_TOP = 10;
    private static final int CHART_TITLE_PADDING_BOTTOM = 8;

    private static final Map<String, Color> CATEGORY_COLORS = new HashMap<>();
    static {
        CATEGORY_COLORS.put("Food", new Color(0x4F46E5));
        CATEGORY_COLORS.put("Transport", new Color(0x6366F1));
        CATEGORY_COLORS.put("Housing", new Color(0x8B5CF6));
        CATEGORY_COLORS.put("Bills", new Color(0x7C3AED));
        CATEGORY_COLORS.put("Shopping", new Color(0xEC4899));
        CATEGORY_COLORS.put("Entertainment", new Color(0xF59E0B));
        CATEGORY_COLORS.put("Healthcare", new Color(0x22C55E));
        CATEGORY_COLORS.put("Education", new Color(0x0EA5E9));
        CATEGORY_COLORS.put("Other", new Color(0x6B7280));
    }

    private ChartUtils() {
    }

    public static Color getCategoryColor(String name) {
        return CATEGORY_COLORS.getOrDefault(name, Color.GRAY);
    }

    public static void applyGeneral(JFreeChart chart) {
        chart.setBackgroundPaint(TRANSPARENT);
        if (chart.getPlot() instanceof CategoryPlot cp) {
            cp.setBackgroundPaint(TRANSPARENT);
            cp.setOutlineVisible(false);
            cp.setDomainGridlinesVisible(false);
            cp.setRangeGridlinesVisible(false);
            trySetAxisLineInvisible(cp.getDomainAxis());
            trySetAxisLineInvisible(cp.getRangeAxis());
            trySetTickLabelStyle(cp.getDomainAxis());
            trySetTickLabelStyle(cp.getRangeAxis());
        } else if (chart.getPlot() instanceof XYPlot xp) {
            xp.setBackgroundPaint(TRANSPARENT);
            xp.setOutlineVisible(false);
            xp.setDomainGridlinesVisible(false);
            xp.setRangeGridlinesVisible(false);
            trySetAxisLineInvisible(xp.getDomainAxis());
            trySetAxisLineInvisible(xp.getRangeAxis());
            trySetTickLabelStyle(xp.getDomainAxis());
            trySetTickLabelStyle(xp.getRangeAxis());
        } else if (chart.getPlot() instanceof PiePlot) {
            chart.getPlot().setBackgroundPaint(TRANSPARENT);
            chart.getPlot().setOutlineVisible(false);
        }
    }

    private static void trySetAxisLineInvisible(org.jfree.chart.axis.Axis axis) {
        if (axis == null)
            return;
        try {
            axis.setAxisLinePaint(TRANSPARENT);
        } catch (Exception ignored) {
        }
    }

    private static void trySetTickLabelStyle(org.jfree.chart.axis.Axis axis) {
        if (axis == null)
            return;
        try {
            axis.setTickLabelFont(TICK_LABEL_FONT);
            axis.setTickLabelPaint(TICK_LABEL_GRAY);
        } catch (Exception ignored) {
        }
    }

    public static void applyBarChart(JFreeChart chart) {
        applyGeneral(chart);
        if (!(chart.getPlot() instanceof CategoryPlot cp))
            return;
        BarRenderer r = (BarRenderer) cp.getRenderer();
        r.setBarPainter(new RoundedBarPainter());
        r.setShadowVisible(false);
        r.setSeriesPaint(0, SOFT_BLUE);
        try {
            if (cp.getRangeAxis() != null)
                cp.getRangeAxis().setTickLabelsVisible(false);
        } catch (Exception ignored) {
        }
    }

    public static void applyDonutChart(JFreeChart chart, PieDataset dataset) {
        applyGeneral(chart);
        if (!(chart.getPlot() instanceof PiePlot pp))
            return;
        pp.setCircular(true);
        pp.setInteriorGap(0.40);
        pp.setBackgroundPaint(TRANSPARENT);
        pp.setOutlineVisible(false);
        pp.setShadowPaint(null);
        try {
            pp.setShadowGenerator(null);
        } catch (Exception ignored) {
        }
        pp.setSectionOutlinesVisible(false);
        for (Object key : dataset.getKeys()) {
            String name = key.toString();
            pp.setSectionPaint((Comparable<?>) key, getCategoryColor(name));
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setPosition(RectangleEdge.RIGHT);
            try {
                chart.getLegend().setFrame((org.jfree.chart.block.BlockFrame) null);
            } catch (Exception ignored) {
            }
        }
    }

    public static void applyLineChart(JFreeChart chart) {
        applyGeneral(chart);
        if (!(chart.getPlot() instanceof XYPlot xp))
            return;
        XYSplineRenderer r = new XYSplineRenderer();
        r.setSeriesPaint(0, TEAL);
        r.setSeriesShapesVisible(0, false);
        r.setSeriesLinesVisible(0, true);
        xp.setRenderer(r);
        if (xp.getDomainAxis() != null)
            xp.getDomainAxis().setVisible(false);
        if (xp.getRangeAxis() != null)
            xp.getRangeAxis().setVisible(false);
    }

    public static ChartPanel createChartPanel(JFreeChart chart) {
        ChartPanel cp = new ChartPanel(chart, 280, 180, 80, 80, 1024, 768, true, true, true, true, true, true);
        cp.setBackground(TRANSPARENT);
        cp.setOpaque(false);
        return cp;
    }

    public static void applyChartTitle(JFreeChart chart, String title) {
        TextTitle textTitle = new TextTitle(title);
        textTitle.setFont(new Font(CHART_TITLE_FONT_FAMILY, Font.PLAIN, CHART_TITLE_FONT_SIZE));
        textTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        textTitle.setPadding(new RectangleInsets(CHART_TITLE_PADDING_TOP, 0, CHART_TITLE_PADDING_BOTTOM, 0));
        chart.setTitle(textTitle);
    }

    public static JLabel createChartTitleLabel(String title) {
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font(CHART_TITLE_FONT_FAMILY, Font.PLAIN, CHART_TITLE_FONT_SIZE));
        return label;
    }

    public static Font getChartTitleFont() {
        return new Font(CHART_TITLE_FONT_FAMILY, Font.PLAIN, CHART_TITLE_FONT_SIZE);
    }
}
