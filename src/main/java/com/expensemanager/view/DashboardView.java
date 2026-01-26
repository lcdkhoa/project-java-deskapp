package com.expensemanager.view;

import com.expensemanager.util.UIFactory;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard - Material Design. Main bg #F3F4F6. Responsive MigLayout: KPI (4 equal), charts (35-30-35), Budget Warnings.
 * Insets 20px, gap 15px. All cards grow and fill. Content tracks viewport width to avoid white space on the right.
 */
public class DashboardView extends JPanel {
    private static final Color MAIN_BG = new Color(0xF3F4F6);
    private static final int INSETS = 20;
    private static final int GAP = 15;

    private final MainFrame main;
    private final DashboardController controller;

    public DashboardView(MainFrame main) {
        this.main = main;
        this.controller = new DashboardController(this);
        setBackground(MAIN_BG);
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(BorderFactory.createEmptyBorder(INSETS, INSETS, INSETS, INSETS));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MAIN_BG);
        top.add(controller.getMonthSelectorPanel(), BorderLayout.CENTER);
        JButton addTx = UIFactory.createPrimaryButton("Add Transaction", UIFactory.createPlusIcon());
        addTx.addActionListener(e -> new CreateTransactionDialog(main).setVisible(true));
        top.add(addTx, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // MigLayout: 4 cols [grow,fill] equal; rows: KPI (min), charts (grow), Budget (min). Gap 15. All grow/push.
        JPanel content = new ScrollableContentPanel(new MigLayout(
                "ins 0, gap " + GAP + " " + GAP,
                "[grow,fill][grow,fill][grow,fill][grow,fill]",
                "[] [grow] []"
        ));
        content.setBackground(MAIN_BG);
        content.add(controller.getKpiCardsPanel(), "span 4, growx, pushx, wrap");
        content.add(controller.getChartsPanel(), "span 4, growx, pushx, growy, wrap");
        content.add(controller.getBudgetWarningsPanel(), "span 4, growx, pushx");

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(MAIN_BG);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * JPanel that implements Scrollable so the content tracks viewport width (no white space on the right).
     */
    private static final class ScrollableContentPanel extends JPanel implements Scrollable {
        ScrollableContentPanel(MigLayout layout) { super(layout); }

        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return (o == SwingConstants.HORIZONTAL) ? r.width : r.height;
        }
    }

    void onShown() { refresh(); }
    void refresh() { controller.refresh(); }

    MainFrame getMain() { return main; }
}
