package space.istef;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class TrayPopup {

    private AppTheme theme;
    private JDialog dialog;

    public TrayPopup(AppTheme initialTheme) {
        this.theme = initialTheme != null ? initialTheme : AppTheme.DARK;
        this.dialog = buildDialog();
    }

    public void applyTheme(AppTheme newTheme) {
        if (newTheme == null) {
            return;
        }
        boolean wasVisible = dialog != null && dialog.isVisible();
        if (dialog != null) {
            dialog.dispose();
        }
        this.theme = newTheme;
        this.dialog = buildDialog();
        if (wasVisible) {
            showNearPointer();
        }
    }

    public AppTheme getTheme() {
        return theme;
    }

    public void showNearPointer() {
        Point pointer = MouseInfo.getPointerInfo().getLocation();
        int x = pointer.x - dialog.getWidth() / 2;
        int y = pointer.y - dialog.getHeight() - 16;
        dialog.setLocation(Math.max(8, x), Math.max(16, y));
        dialog.setVisible(true);
        dialog.toFront();
    }

    public void hide() {
        dialog.setVisible(false);
    }

    public void dispose() {
        if (dialog != null) {
            dialog.dispose();
        }
    }

    private JDialog buildDialog() {
        JDialog popup = new JDialog((Frame) null);
        popup.setUndecorated(true);
        popup.setAlwaysOnTop(true);
        popup.setType(Window.Type.POPUP);
        popup.getContentPane().setBackground(theme.surface);

        int arc = 7;

        JPanel content = getJPanel(arc);
        popup.setContentPane(content);

        JButton settingsBtn = createStyledButton(Messages.MENU_SETTINGS, theme,() -> {
            popup.setVisible(false);
            onSettingsClicked();
        });

        JButton uninstallBtn = createStyledButton(Messages.MENU_UNINSTALL, theme, () -> {
            popup.setVisible(false);
            onUninstallClicked();
        });

        JButton quitBtn = createStyledButton(Messages.MENU_QUIT,theme, () -> {
            popup.dispose();
            System.exit(0);
        });

        addMenuItem(content, settingsBtn);
        content.add(createHorizontalSeparator(theme, 2));
        addMenuItem(content, uninstallBtn);
        content.add(createHorizontalSeparator(theme,2));
        addMenuItem(content, quitBtn);

        popup.pack();
        if (popup.getWidth() > 0 && popup.getHeight() > 0) {
            popup.setShape(new RoundRectangle2D.Float(0, 0, popup.getWidth(), popup.getHeight(), arc, arc));
        }

        popup.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                popup.setVisible(false);
            }
        });

        return popup;
    }

    private JPanel getJPanel(int arc) {
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(theme.border);
                    // Inset by 1px and subtract 2 from width/height so the stroke stays inside setShape
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
                } finally {
                    g2.dispose();
                }
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(true);
        content.setBackground(theme.surface);
        content.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        return content;
    }

    private void onSettingsClicked() {
        // Your settings action logic here
    }

    private void onUninstallClicked() {
        // Your uninstall/cleanup action logic here
    }

    private static void addMenuItem(JPanel container, JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        container.add(button);
    }

    private static JButton createStyledButton(String text, AppTheme activeTheme, Runnable onClick) {
        Color normalBg = activeTheme.buttonBg;
        Color hoverBg = activeTheme.buttonHoverBg;

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setForeground(activeTheme.buttonFg);
        button.setBackground(normalBg);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activeTheme.buttonBorder, 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalBg);
                button.repaint();
            }
        });

        button.addActionListener(e -> {
            button.setBackground(normalBg);
            onClick.run();
        });

        return button;
    }

    private static JPanel createHorizontalSeparator(AppTheme activeTheme, int verticalGap) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(verticalGap, 4, verticalGap, 4));

        JPanel line = new JPanel();
        line.setOpaque(true);
        line.setBackground(activeTheme.border);
        line.setPreferredSize(new Dimension(0, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        wrapper.add(line, BorderLayout.CENTER);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
        return wrapper;
    }
}