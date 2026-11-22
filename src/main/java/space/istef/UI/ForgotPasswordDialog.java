package space.istef.UI;

import space.istef.Messages;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class ForgotPasswordDialog {

    private static final int CARD_WIDTH = 320;
    private static final int CARD_HEIGHT = 120;
    private static final int CARD_ARC = CARD_HEIGHT / 10;
    private static final int ROW_GAP = 8;
    private static final int LABEL_WIDTH = 52;
    private static final int INPUT_WIDTH = 190;
    private static final int INPUT_HEIGHT = 28;
    private static final int ROW_CONTENT_WIDTH = LABEL_WIDTH + ROW_GAP + INPUT_WIDTH;
    private static final int BUTTON_HEIGHT = 30;

    private final AppTheme theme;
    private final JDialog dialog;
    private JTextField emailField;
    private JButton submitButton;
    private JLabel statusLabel;

    public ForgotPasswordDialog(Window owner, AppTheme theme) {
        this.theme = theme != null ? theme : AppTheme.DARK;
        this.dialog = buildDialog(owner);
    }

    public void showDialog() {
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    private JDialog buildDialog(Window owner) {
        JDialog d = new JDialog(owner, "Забравена парола", JDialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        d.getContentPane().setBackground(theme.surface);

        JPanel card = UI.createCardPanel(
                theme,
                CARD_WIDTH,
                CARD_HEIGHT,
                22,
                28,
                14,
                2
        );

        emailField = UI.createTextField(theme, INPUT_WIDTH, INPUT_HEIGHT);
        JPanel emailRow = createEmailRow(emailField);
        JPanel actionRow = createActionRow();

        card.add(emailRow);
        card.add(Box.createVerticalStrut(14));
        card.add(actionRow);

        UI.configureRoundedDialog(d, card, CARD_ARC, submitButton, d::dispose);
        return d;
    }

    private JPanel createEmailRow(JTextField field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, ROW_GAP, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(Messages.LABEL_EMAIL);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        label.setForeground(theme.buttonFg);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, INPUT_HEIGHT));

        row.add(label);
        row.add(field);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT + 4));
        return row;
    }

    private JPanel createActionRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, ROW_GAP, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);

        submitButton = UI.createOutlineButton(
                theme,
                Messages.BUTTON_NEW_PASSWORD,
                ROW_CONTENT_WIDTH,
                BUTTON_HEIGHT
        );
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> handleRequestReset());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        statusLabel.setForeground(theme.errorColor);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        column.add(submitButton);
        column.add(Box.createVerticalStrut(4));
        column.add(statusLabel);

        row.add(column);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT + 22));
        return row;
    }

    private void handleRequestReset() {
        String email = emailField.getText().trim();
        if (!email.contains("@")) {
            statusLabel.setForeground(theme.errorColor);
            statusLabel.setText(Messages.STATUS_ERROR_INVALID_EMAIL);
            return;
        }

        statusLabel.setForeground(theme.buttonFg);
        statusLabel.setText(Messages.STATUS_SENDING);
        submitButton.setEnabled(false);

        Timer timer = new Timer(1000, e -> SwingUtilities.invokeLater(() -> {
            dialog.dispose();
            System.exit(0);
        }));
        timer.setRepeats(false);
        timer.start();
    }
}