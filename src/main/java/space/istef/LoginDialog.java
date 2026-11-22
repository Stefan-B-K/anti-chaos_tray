package space.istef;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.InputStream;
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

public class LoginDialog {

    private static final int CARD_WIDTH = 320;
    private static final int CARD_HEIGHT = 180;
    private static final int CARD_ARC = 18;
    private static final int INPUT_WIDTH = 190;
    private static final int INPUT_HEIGHT = 28;
    private static final int BUTTON_WIDTH = 190;
    private static final int BUTTON_HEIGHT = 32;

    private final JDialog dialog;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JButton loginButton;
    private boolean authenticated = false;
    private final AppTheme theme;

    public LoginDialog(AppTheme theme) {
        this.theme = theme != null ? theme : AppTheme.DARK;
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

        JPanel card = getJPanel();

        usernameField = createStyledTextField();
        passwordField = createStyledPasswordField();

        JPanel usernameRow = createInputRow(Messages.LABEL_USERNAME, usernameField);
        JPanel passwordRow = createInputRow(Messages.LABEL_PASSWORD, passwordField);
        JPanel actionRow = createActionRow();

        card.add(usernameRow);
        card.add(Box.createVerticalStrut(14));
        card.add(passwordRow);
        card.add(Box.createVerticalStrut(14));
        card.add(actionRow);

        d.setContentPane(card);
        d.pack();
        if (d.getWidth() > 0 && d.getHeight() > 0) {
            d.setShape(new RoundRectangle2D.Float(0, 0, d.getWidth(), d.getHeight(), CARD_ARC, CARD_ARC));
        }

        d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                authenticated = false;
                d.dispose();
            }
        });

        d.getRootPane().setDefaultButton(loginButton);
        return d;
    }

    private JPanel getJPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(theme.surface);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
                    g2.setColor(theme.border);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(-1, -1, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
                    g2.setColor(theme.buttonHoverBg);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(1, 1, getWidth(), getHeight(), CARD_ARC, CARD_ARC);
                } finally {
                    g2.dispose();
                }
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setBorder(BorderFactory.createEmptyBorder(18, 2, 2, 2));
        return card;
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

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        applyFieldStyle(field);
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        applyFieldStyle(field);
        return field;
    }

    private void applyFieldStyle(JTextField field) {
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        field.setForeground(theme.buttonFg);
        field.setBackground(theme.inputBg);
        field.setCaretColor(theme.buttonFg);
        field.setPreferredSize(new Dimension(INPUT_WIDTH, INPUT_HEIGHT));
        field.setMinimumSize(new Dimension(INPUT_WIDTH, INPUT_HEIGHT));
        field.setMaximumSize(new Dimension(INPUT_WIDTH, INPUT_HEIGHT));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(theme.inputBorder, 1, 12),
                BorderFactory.createEmptyBorder(3, 7, 3, 7)
        ));
    }

    private JPanel createActionRow() {
        JPanel row = new JPanel(new BorderLayout(0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoLabel = createLogoLabel();
        logoLabel.setPreferredSize(new Dimension(92, 58));

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        errorLabel.setForeground(theme.errorColor);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setPreferredSize(new Dimension(BUTTON_WIDTH, 12));
        errorLabel.setMaximumSize(new Dimension(BUTTON_WIDTH, 12));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton = createOutlineButton(Messages.BUTTON_LOGIN);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        JLabel forgotLink = new JLabel(Messages.LABEL_FORGOT_PASSWORD);
        forgotLink.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        forgotLink.setForeground(theme.buttonFg);
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotLink.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 2));
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                errorLabel.setText("Reset link not configured");
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

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (isEnabled()) {
                            hovered = true;
                            repaint();
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (hovered && isEnabled()) {
                        g2.setColor(theme.buttonHoverBg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    }
                    g2.setColor(isEnabled() ? theme.buttonFg : theme.buttonFg.darker());
                    g2.setStroke(new BasicStroke(1.1f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        btn.setForeground(theme.buttonFg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        btn.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        btn.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        return btn;
    }

    private void handleLogin() {
        String user = usernameField.getText().trim();
        char[] pass = passwordField.getPassword();

        if (user.isEmpty() || pass.length == 0) {
            errorLabel.setText("Enter username and password");
            java.util.Arrays.fill(pass, '\0');
            return;
        }

        java.util.Arrays.fill(pass, '\0');
        errorLabel.setText(" ");
        loginButton.setEnabled(false);
        loginButton.setText("...");

        // Simulate brief login check, then close dialog and start tray app
        Timer timer = new Timer(250, e -> SwingUtilities.invokeLater(() -> {
            authenticated = true;
            dialog.dispose();
        }));
        timer.setRepeats(false);
        timer.start();
    }
}