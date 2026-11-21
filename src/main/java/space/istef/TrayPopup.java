package space.istef;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class TrayPopup {

    private static final int ARC = 7;

    private AppTheme theme;
    private final JDialog dialog;
    private JPanel contentPanel;
    private final List<JButton> menuButtons = new ArrayList<>();
    private final List<JPanel> separatorLines = new ArrayList<>();
    private final Consumer<Boolean> onVisibilityChanged;

    private SettingsPopup settingsPopup;
    private boolean notificationsEnabled = true;

    public TrayPopup(AppTheme initialTheme, Consumer<Boolean> onVisibilityChanged) {
        this.theme = initialTheme != null ? initialTheme : AppTheme.DARK;
        this.onVisibilityChanged = onVisibilityChanged;
        this.dialog = buildDialog();
    }

    public void applyTheme(AppTheme newTheme) {
        if (newTheme == null) {
            return;
        }
        this.theme = newTheme;
        dialog.getContentPane().setBackground(theme.surface);
        if (contentPanel != null) {
            contentPanel.setBackground(theme.surface);
        }
        for (JButton btn : menuButtons) {
            btn.setForeground(theme.buttonFg);
            btn.setBackground(theme.buttonBg);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(theme.buttonBg, 1, true),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
        }
        for (JPanel line : separatorLines) {
            line.setBackground(theme.border);
        }
        if (dialog.getWidth() > 0 && dialog.getHeight() > 0) {
            dialog.setShape(new RoundRectangle2D.Float(0, 0, dialog.getWidth(), dialog.getHeight(), ARC, ARC));
        }
        dialog.repaint();

        if (settingsPopup != null) {
            settingsPopup.applyTheme(this.theme);
        }
    }

    public AppTheme getTheme() {
        return theme;
    }

    private void notifyVisibility(boolean visible) {
        if (onVisibilityChanged != null) {
            onVisibilityChanged.accept(visible);
        }
    }

    public void showNearPointer() {
        Point pointer = MouseInfo.getPointerInfo().getLocation();
        int x = pointer.x - dialog.getWidth() / 2;
        int y = pointer.y - dialog.getHeight() - 16;
        dialog.setLocation(Math.max(8, x), Math.max(16, y));
        notifyVisibility(true);
        dialog.setVisible(true);
        dialog.toFront();
    }

    public void hide() {
        if (settingsPopup != null) {
            settingsPopup.hide();
        }
        dialog.setVisible(false);
        notifyVisibility(false);
    }

    public void dispose() {
        if (settingsPopup != null) {
            settingsPopup.dispose();
        }
        if (dialog != null) {
            dialog.dispose();
        }
        notifyVisibility(false);
    }

    private JDialog buildDialog() {
        menuButtons.clear();
        separatorLines.clear();

        JDialog popup = new JDialog((Frame) null);
        popup.setUndecorated(true);
        popup.setAlwaysOnTop(true);
        popup.setType(Window.Type.POPUP);
        popup.getContentPane().setBackground(theme.surface);

        contentPanel = getJPanel(ARC);
        popup.setContentPane(contentPanel);

        // Keep TrayPopup visible when Settings opens on top of it
        JButton settingsBtn = createStyledButton(Messages.MENU_SETTINGS, this::onSettingsClicked);
        JButton uninstallBtn = createStyledButton(Messages.MENU_UNINSTALL, () -> {
            hide();
            onUninstallClicked();
        });
        JButton quitBtn = createStyledButton(Messages.MENU_QUIT, () -> {
            dispose();
            System.exit(0);
        });

        menuButtons.add(settingsBtn);
        menuButtons.add(uninstallBtn);
        menuButtons.add(quitBtn);

        addMenuItem(contentPanel, settingsBtn);
        contentPanel.add(createHorizontalSeparator(2));
        addMenuItem(contentPanel, uninstallBtn);
        contentPanel.add(createHorizontalSeparator(2));
        addMenuItem(contentPanel, quitBtn);

        popup.pack();
        if (popup.getWidth() > 0 && popup.getHeight() > 0) {
            popup.setShape(new RoundRectangle2D.Float(0, 0, popup.getWidth(), popup.getHeight(), ARC, ARC));
        }

        popup.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                hide();
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
        if (settingsPopup == null) {
            settingsPopup = new SettingsPopup(
                    theme,
                    theme.mode == AppTheme.Mode.DARK,
                    notificationsEnabled,
                    darkSelected -> {
                        AppTheme.Mode nextMode = darkSelected ? AppTheme.Mode.DARK : AppTheme.Mode.LIGHT;
                        AppTheme.saveMode(nextMode);
                        applyTheme(AppTheme.of(nextMode));
                    },
                    notifSelected -> {
                        notificationsEnabled = notifSelected;
                    }
            );
        } else {
            settingsPopup.applyTheme(theme);
        }
        settingsPopup.showAlignedTopLeft(dialog);
    }

    private void onUninstallClicked() {
        // Your uninstall/cleanup action logic here
    }

    private static void addMenuItem(JPanel container, JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        container.add(button);
    }

    private JButton createStyledButton(String text, Runnable onClick) {
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
        button.setForeground(theme.buttonFg);
        button.setBackground(theme.buttonBg);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.buttonBg, 1, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(theme.buttonHoverBg);
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(theme.buttonBg);
                button.repaint();
            }
        });

        button.addActionListener(e -> {
            button.setBackground(theme.buttonBg);
            onClick.run();
        });

        return button;
    }

    private JPanel createHorizontalSeparator(int verticalGap) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(verticalGap, 4, verticalGap, 4));

        JPanel line = new JPanel();
        line.setOpaque(true);
        line.setBackground(theme.border);
        line.setPreferredSize(new Dimension(0, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        separatorLines.add(line);
        wrapper.add(line, BorderLayout.CENTER);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
        return wrapper;
    }
}