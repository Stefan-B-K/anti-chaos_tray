package space.istef.UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import space.istef.Messages;
import space.istef.auth.AuthService;

public class LoginDialog {

    private static final int CARD_WIDTH = 340;
    private static final int CARD_HEIGHT = 190;
    private static final int CARD_ARC = CARD_HEIGHT / 10;
    private static final int INPUT_WIDTH = 190;
    private static final int INPUT_HEIGHT = 28;
    private static final int BUTTON_WIDTH = 190;
    private static final int BUTTON_HEIGHT = 30;

    private final JDialog dialog;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JButton loginButton;
    private boolean authenticated = false;
    private final AppTheme theme;
    private final AuthService authService;

    private final Font baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private final Font underlinedFont = baseFont.deriveFont(underlinedAttrs());

    public LoginDialog(AppTheme theme, AuthService authService) {
        this.theme = theme != null ? theme : AppTheme.DARK;
        this.authService = authService;
        this.dialog = buildDialog();
    }

    /**
     * Shows the login dialog modally and returns true if login succeeded.
     */
    public boolean showLoginDialog() {
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return authenticated;
    }

    private JDialog buildDialog() {
        JDialog d = new JDialog((Frame) null, "Login", true);
        d.setUndecorated(true);
        d.getContentPane().setBackground(theme.surface);

        JPanel card = UI.createCardPanel(
                theme,
                CARD_WIDTH,
                CARD_HEIGHT,
                6,
                2,
                2,
                2
        );

        usernameField = UI.createTextField(theme, INPUT_WIDTH, INPUT_HEIGHT);
        passwordField = UI.createPasswordField(theme, INPUT_WIDTH, INPUT_HEIGHT);

        JPanel usernameRow = createInputRow(Messages.LABEL_USERNAME, usernameField);
        JPanel passwordRow = createInputRow(Messages.LABEL_PASSWORD, passwordField);
        JPanel actionRow = createActionRow();

        card.add(UI.createCloseHeader(d, theme));
        card.add(Box.createVerticalStrut(0));
        card.add(usernameRow);
        card.add(Box.createVerticalStrut(14));
        card.add(passwordRow);
        card.add(Box.createVerticalStrut(14));
        card.add(actionRow);

        UI.configureRoundedDialog(d, card, CARD_ARC, loginButton, () -> {
            authenticated = false;
            d.dispose();
        });
        return d;
    }

    private JPanel createInputRow(String labelText, JTextField field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        label.setForeground(theme.buttonFg);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new Dimension(92, INPUT_HEIGHT));

        row.add(label);
        row.add(field);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, INPUT_HEIGHT + 4));
        return row;
    }

    private JPanel createActionRow() {
        JPanel row = new JPanel(new BorderLayout(-20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoLabel = createLogoLabel();
        logoLabel.setPreferredSize(new Dimension(92, 58));

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightCol.setOpaque(false);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        errorLabel.setForeground(theme.errorColor);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setPreferredSize(new Dimension(BUTTON_WIDTH, 11));
        errorLabel.setMaximumSize(new Dimension(BUTTON_WIDTH, 11));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton = UI.createOutlineButton(theme, Messages.BUTTON_LOGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        JLabel forgotLink = new JLabel(Messages.LABEL_FORGOT_PASSWORD);
        forgotLink.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        forgotLink.setForeground(theme.buttonFg);
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotLink.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        forgotLink.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 2));
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotLink.setFont(underlinedFont);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotLink.setFont(baseFont);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
                new ForgotPasswordDialog(dialog, theme).showDialog();
            }
        });

        rightCol.add(errorLabel);
        rightCol.add(Box.createVerticalStrut(3));
        rightCol.add(loginButton);
        rightCol.add(forgotLink);

        row.add(logoLabel, BorderLayout.LINE_START);
        row.add(rightCol, BorderLayout.CENTER);
        return row;
    }

    private JLabel createLogoLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setVerticalAlignment(SwingConstants.TOP);
        try (InputStream in = LoginDialog.class.getResourceAsStream(Messages.ICON_RESOURCE_PATH)) {
            if (in != null) {
                Image img = ImageIO.read(in);
                if (img != null) {
                    Image scaled = img.getScaledInstance(72, 64, Image.SCALE_SMOOTH);
                    label.setIcon(new javax.swing.ImageIcon(scaled));
                }
            }
        } catch (IOException ignored) {
            label.setText("");
        }
        return label;
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        char[] pass = passwordField.getPassword();

        if (user.isEmpty() || pass.length == 0) {
            errorLabel.setText(Messages.ERROR_EMPTY_INPUT);
            java.util.Arrays.fill(pass, '\0');
            return;
        }

        String password = new String(pass);
        java.util.Arrays.fill(pass, '\0');
        errorLabel.setText(" ");
        loginButton.setEnabled(false);
        loginButton.setText("...");

        // Perform actual login via AuthService
        new Thread(() -> {
            boolean success = authService.login(user, password);
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    authenticated = true;
                    dialog.dispose();
                } else {
                    errorLabel.setText("Login failed. Please check your credentials.");
                    loginButton.setEnabled(true);
                    loginButton.setText(Messages.BUTTON_LOGIN);
                }
            });
        }).start();
    }

    private Map<TextAttribute, Object> underlinedAttrs() {
        Map<TextAttribute, Object> underlinedAttrs = new HashMap<>(baseFont.getAttributes());
        underlinedAttrs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        return underlinedAttrs;
    }
}