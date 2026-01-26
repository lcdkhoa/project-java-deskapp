package com.expensemanager.view;

import com.expensemanager.util.UIFactory;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard - Figma layout: MigLayout wrap 1, insets 0.
 * Row 1: Header (White) Title + Add Transaction.
 * Row 2: Date Selector (transparent row, white rounded card, arc 20).
 * Row 3: KPI cards. Row 4: Charts (growy, pushy to fill). Row 5: Budget Warning (South).
 * Light mode only. Main BG White (Color.WHITE).
 */
public class DashboardView extends JPanel {
    private static final Color MAIN_BG = Color.WHITE; // Changed from Light Gray to White
    private static final Color SUBTITLE_GRAY = new Color(0x6B7280);
    private static final Color CARD_BORDER = new Color(229, 231, 235); // #E5E7EB
    private static final int CARD_GAP = 20;
    private static final int HEADER_HEIGHT = 70; // 60–80px
    private static final int DATE_STRIP_HEIGHT = 70;
    private static final int CARD_ARC = 30;

    private final MainFrame main;
    private final DashboardController controller;

    public DashboardView(MainFrame main) {
        this.main = main;
        this.controller = new DashboardController(this);
        setBackground(MAIN_BG);
        setOpaque(true); // Ensure panel is opaque to show white background
        setLayout(new MigLayout("ins 0, wrap 1, gap " + CARD_GAP + " " + CARD_GAP, "[grow,fill]", "[] [][][grow,fill] []"));
        
        // Ensure any parent ScrollPane viewport also has white background
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

        // Row 1 (Header): White BG, ~60–80px. Title left, + Add Transaction right.
        JPanel header = buildHeaderPanel();
        add(header, "growx, h " + HEADER_HEIGHT + ", wrap");

        // Row 2 (Date Selector): Transparent row, white rounded card (arc 20) centered.
        add(buildDateSelectorStrip(), "growx, wrap");

        // Row 3 (KPI Cards)
        add(controller.getKpiCardsPanel(), "growx, wrap");

        // Row 4 (Charts): growy, pushy to fill and touch Budget Warning
        add(controller.getChartsPanel(), "growx, growy, pushy, wrap");

        // Row 5 (Footer): Budget Warning at bottom
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

        JButton addTx = UIFactory.createPrimaryButton("+ Add Transaction");
        addTx.addActionListener(e -> new CreateTransactionDialog(main).setVisible(true));
        header.add(addTx, "aligny center");
        return header;
    }

    private JPanel buildDateSelectorStrip() {
        // Date selector with MigLayout: buttons at edges, label centered
        JPanel inner = controller.getMonthSelectorPanel();
        return new DateStripCard(inner);
    }

    /** White date selector card: #FFFFFF, arc 30, 1px border #E5E7EB, height 70. */
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

    void onShown() { refresh(); }
    void refresh() { controller.refresh(); }

    MainFrame getMain() { return main; }
}
