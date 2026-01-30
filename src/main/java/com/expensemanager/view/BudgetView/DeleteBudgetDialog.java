package com.expensemanager.view.BudgetView;

import com.expensemanager.view.CommonComponents.StyledComponents;
import com.expensemanager.view.CommonComponents.MainFrame;
import com.expensemanager.view.CommonComponents.StyledComponents;

import javax.swing.*;
import java.awt.*;

public class DeleteBudgetDialog extends JDialog {

    private static final int BUTTON_WIDTH = 110;
    private static final int WARN_ICON_SIZE = 40;
    private static final String WARN_ICON_PATH = "src/main/java/com/expensemanager/img/dashboard/warn.png";

    public DeleteBudgetDialog(MainFrame main, Runnable onConfirm) {
        super(main, "Delete Budget", true);
        setSize(380, 200);
        setLocationRelativeTo(main);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));

        JPanel messageRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 16, 0));
        messageRow.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel();
        ImageIcon warnIcon = StyledComponents.getIcon(WARN_ICON_PATH, WARN_ICON_SIZE, WARN_ICON_SIZE);
        if (warnIcon != null) {
            iconLabel.setIcon(warnIcon);
        }
        messageRow.add(iconLabel);

        JLabel messageLabel = new JLabel("Are you sure to delete this Budget");
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 14f));
        messageLabel.setForeground(new Color(0x111827));
        messageRow.add(messageLabel);

        content.add(messageRow, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttons.setBackground(Color.WHITE);

        JButton cancel = StyledComponents.createSecondaryFunctionButton("Cancel", BUTTON_WIDTH);
        cancel.addActionListener(e -> dispose());

        JButton yes = StyledComponents.createStyledButton("Yes", null,
                StyledComponents.ButtonType.DANGER, StyledComponents.ButtonSize.FUNCTION, BUTTON_WIDTH);
        yes.addActionListener(e -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
            dispose();
        });
        buttons.add(yes);
        buttons.add(cancel);
        content.add(buttons, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }
}
