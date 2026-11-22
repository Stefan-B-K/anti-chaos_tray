package space.istef.UI;

import space.istef.Messages;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SettingsPopup {

    private static final int ARC = 7;

    private AppTheme theme;
    private final JDialog dialog;
    private JPanel contentPanel;
    private JLabel darkModeLabel;
    private JLabel notificationsLabel;
    private JCheckBox darkModeCheckbox;
    private JCheckBox notificationsCheckbox;

    private boolean darkModeEnabled;
    private boolean notificationsEnabled;
    private final Consumer<Boolean> onDarkModeToggled;
    private final Consumer<Boolean> onNotificationsToggled;

    public SettingsPopup(
            AppTheme initialTheme,
            boolean initialDarkMode,
            boolean initialNotifications,
            Consumer<Boolean> onDarkModeToggled,
            Consumer<Boolean> onNotificationsToggled
    ) {
        this.theme = initialTheme != null ? initialTheme : AppTheme.DARK;
        this.darkModeEnabled = initialDarkMode;
        this.notificationsEnabled = initialNotifications;
        this.onDarkModeToggled = onDarkModeToggled;
        this.onNotificationsToggled = onNotificationsToggled;
        this.dialog = buildDialog();
    }

    public void showAlignedTopLeft(Window anchorWindow) {
        if (anchorWindow != null && anchorWindow.isShowing()) {
            Point loc = anchorWindow.getLocationOnScreen();
            dialog.setLocation(Math.max(8, loc.x), Math.max(16, loc.y));
        } else if (anchorWindow != null) {
            Point loc = anchorWindow.getLocation();
            if (loc.x > 0 || loc.y > 0) {
                dialog.setLocation(Math.max(8, loc.x), Math.max(16, loc.y));
            }
        }
        dialog.setVisible(true);
        dialog.toFront();
    }

    public boolean isVisible() {
        return dialog != null && dialog.isVisible();
    }

    public void hide() {
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }

    public void dispose() {
        if (dialog != null) {
            dialog.dispose();
        }
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
        if (darkModeLabel != null) {
            darkModeLabel.setForeground(theme.buttonFg);
        }
        if (notificationsLabel != null) {
            notificationsLabel.setForeground(theme.buttonFg);
        }
        if (darkModeCheckbox != null) {
            darkModeCheckbox.repaint();
        }
        if (notificationsCheckbox != null) {
            notificationsCheckbox.repaint();
        }
        if (dialog.getWidth() > 0 && dialog.getHeight() > 0) {
            dialog.setShape(new RoundRectangle2D.Float(0, 0, dialog.getWidth(), dialog.getHeight(), ARC, ARC));
        }
        dialog.repaint();
    }

    private JDialog buildDialog() {
        JDialog popup = new JDialog((Frame) null);
        popup.setUndecorated(true);
        popup.setAlwaysOnTop(true);
        popup.setFocusableWindowState(false);
        popup.setType(Window.Type.POPUP);
        popup.getContentPane().setBackground(theme.surface);

        contentPanel = createRoundedSurfacePanel();
        popup.setContentPane(contentPanel);

        darkModeCheckbox = createStyledCheckbox(darkModeEnabled);
        darkModeLabel = createRowLabel(Messages.MENU_DARK_MODE, darkModeCheckbox);
        JPanel darkModeRow = assembleRow(darkModeCheckbox, darkModeLabel, selected -> {
            darkModeEnabled = selected;
            if (onDarkModeToggled != null) {
                onDarkModeToggled.accept(selected);
            }
        });

        notificationsCheckbox = createStyledCheckbox(notificationsEnabled);
        notificationsLabel = createRowLabel(Messages.MENU_NOTIFICATIONS, notificationsCheckbox);
        JPanel notificationsRow = assembleRow(notificationsCheckbox, notificationsLabel, selected -> {
            notificationsEnabled = selected;
            if (onNotificationsToggled != null) {
                onNotificationsToggled.accept(selected);
            }
        });

        contentPanel.add(darkModeRow);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(notificationsRow);

        popup.pack();
        if (popup.getWidth() > 0 && popup.getHeight() > 0) {
            popup.setShape(new RoundRectangle2D.Float(0, 0, popup.getWidth(), popup.getHeight(), ARC, ARC));
        }

        return popup;
    }

    private JPanel createRoundedSurfacePanel() {
        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(theme.border);
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, ARC, ARC);
                } finally {
                    g2.dispose();
                }
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(true);
        content.setBackground(theme.surface);
        content.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return content;
    }

    private JLabel createRowLabel(String text, JCheckBox targetCheckbox) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        label.setForeground(theme.buttonFg);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                targetCheckbox.doClick();
            }
        });
        return label;
    }

    private JPanel assembleRow(JCheckBox checkbox, JLabel label, Consumer<Boolean> onChange) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        checkbox.addActionListener(e -> {
            if (onChange != null) {
                onChange.accept(checkbox.isSelected());
            }
        });

        row.add(checkbox);
        row.add(label);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private JCheckBox createStyledCheckbox(boolean selected) {
        int size = 18;

        JCheckBox checkbox = new JCheckBox("", selected) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int boxSize = Math.min(getWidth(), getHeight()) - 2;
                    int x = 1;
                    int y = (getHeight() - boxSize) / 2;

                    if (isSelected()) {
                        g2.setColor(theme.buttonBg);
                        g2.fillRoundRect(x, y, boxSize, boxSize, 4, 4);
                        g2.setColor(theme.buttonBorder);
                        g2.drawRoundRect(x, y, boxSize, boxSize, 4, 4);

                        // Crisp checkmark
                        g2.setColor(theme.buttonFg);
                        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        int x1 = x + Math.round(boxSize * 0.24f);
                        int y1 = y + Math.round(boxSize * 0.52f);
                        int x2 = x + Math.round(boxSize * 0.43f);
                        int y2 = y + Math.round(boxSize * 0.72f);
                        int x3 = x + Math.round(boxSize * 0.76f);
                        int y3 = y + Math.round(boxSize * 0.30f);
                        g2.drawLine(x1, y1, x2, y2);
                        g2.drawLine(x2, y2, x3, y3);
                    } else {
                        g2.setColor(theme.surface);
                        g2.fillRoundRect(x, y, boxSize, boxSize, 4, 4);
                        g2.setColor(theme.border);
                        g2.drawRoundRect(x, y, boxSize, boxSize, 4, 4);
                    }
                } finally {
                    g2.dispose();
                }
            }
        };

        checkbox.setPreferredSize(new Dimension(size, size));
        checkbox.setMinimumSize(new Dimension(size, size));
        checkbox.setMaximumSize(new Dimension(size, size));
        checkbox.setOpaque(false);
        checkbox.setContentAreaFilled(false);
        checkbox.setBorderPainted(false);
        checkbox.setFocusPainted(false);
        checkbox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return checkbox;
    }
}