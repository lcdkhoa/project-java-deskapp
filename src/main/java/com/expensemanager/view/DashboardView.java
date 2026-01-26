package com.expensemanager.view;

import com.expensemanager.util.UIFactory;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard - Matches Figma: Header (Title+Subtitle | + Add Transaction), Date bar (white card),
 * KPI + Charts (cards with light border, 20px padding, 20px gap), Budget Warnings docked at bottom.
 */
public class DashboardView extends JPanel {
    private static final Color MAIN_BG = new Color(0xF3F4F6);
    private static final Color SUBTITLE_GRAY = new Color(0x6B7280);
    private static final int INSETS = 20;
    private static final int CARD_GAP = 20;
    private static final int DATE_STRIP_HEIGHT = 50;
    private static final int DATE_STRIP_ARC = 12;

    private final MainFrame main;
    private final DashboardController controller;

    public DashboardView(MainFrame main) {
        this.main = main;
        this.controller = new DashboardController(this);
        setBackground(MAIN_BG);
        setLayout(new BorderLayout(0, CARD_GAP));
        setBorder(BorderFactory.createEmptyBorder(INSETS, INSETS, INSETS, INSETS));

        // 1. Header: Left = Title + Subtitle; Right = + Add Transaction, vertically centered
        JPanel header = buildHeaderPanel();
        // 2. Date Selector: own row, white JPanel (card), 50px, arc 12, content centered
        JPanel dateStrip = buildDateSelectorStrip();

        JPanel top = new JPanel(new MigLayout("ins 0, gap 0 " + CARD_GAP + ", flowy", "[grow,fill]", "[]"));
        top.setBackground(MAIN_BG);
        top.add(header, "growx, wrap");
        top.add(dateStrip, "growx");
        add(top, BorderLayout.NORTH);

        // 3. Content: KPI + Charts, gap 20, cards with light border and 20px internal padding
        JPanel content = buildContentPanel();
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(MAIN_BG);
        add(scroll, BorderLayout.CENTER);

        // 4. Footer: Budget Warnings docked at absolute bottom (south)
        add(controller.getBudgetWarningsPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new MigLayout("ins 0, gap 0", "[grow,fill][]", "[center]"));
        header.setBackground(MAIN_BG);

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

        JButton addTx = UIFactory.createPrimaryButton("+ Add Transaction");
        addTx.addActionListener(e -> new CreateTransactionDialog(main).setVisible(true));
        header.add(addTx, "aligny center");
        return header;
    }

    private JPanel buildDateSelectorStrip() {
        JPanel strip = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width, DATE_STRIP_HEIGHT);
            }
        };
        strip.setOpaque(false);
        strip.setBackground(Color.WHITE);
        JPanel inner = controller.getMonthSelectorPanel();
        inner.setOpaque(false);
        strip.add(inner, BorderLayout.CENTER);

        return new DateStripCard(strip);
    }

    /** White card strip: #FFFFFF, height 50px, arc 12, for date selector. */
    private static final class DateStripCard extends JPanel {
        DateStripCard(JPanel content) {
            setLayout(new BorderLayout());
            setOpaque(false);
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
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), DATE_STRIP_ARC, DATE_STRIP_ARC);
            g2.dispose();
        }
    }

    private JPanel buildContentPanel() {
        // MigLayout: 4 equal cols for KPI; 3 cols (35-30-35) for charts. Gap 20. Cards have internal padding via ModernCard.
        JPanel content = new ScrollableContentPanel(new MigLayout(
                "ins 0, gap " + CARD_GAP + " " + CARD_GAP,
                "[grow,fill][grow,fill][grow,fill][grow,fill]",
                "[] [grow]"
        ));
        content.setBackground(MAIN_BG);
        content.add(controller.getKpiCardsPanel(), "span 4, growx, wrap");
        content.add(controller.getChartsPanel(), "span 4, growx, growy");
        return content;
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
