package space.istef.UI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

public final class UI {

    private UI() {
    }

    public static JPanel createCardPanel(
            AppTheme theme,
            int width,
            int height,
            int topInset,
            int leftInset,
            int bottomInset,
            int rightInset
    ) {
        final int arc = height/ 10;
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(theme.surface);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                    g2.setColor(theme.border);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(-1, -1, getWidth(), getHeight(), arc, arc);
                    g2.setColor(theme.buttonHoverBg);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(1, 1, getWidth(), getHeight(), arc, arc);
                } finally {
                    g2.dispose();
                }
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, height));
        card.setBorder(BorderFactory.createEmptyBorder(topInset, leftInset, bottomInset, rightInset));
        return card;
    }

    public static JTextField createTextField(AppTheme theme, int width, int height) {
        JTextField field = new JTextField();
        applyFieldStyle(field, theme, width, height);
        return field;
    }

    public static JPasswordField createPasswordField(AppTheme theme, int width, int height) {
        JPasswordField field = new JPasswordField();
        applyFieldStyle(field, theme, width, height);
        return field;
    }

    public static void applyFieldStyle(JTextField field, AppTheme theme, int width, int height) {
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        field.setForeground(theme.buttonFg);
        field.setBackground(theme.inputBg);
        field.setCaretColor(theme.buttonFg);
        field.setPreferredSize(new Dimension(width, height));
        field.setMinimumSize(new Dimension(width, height));
        field.setMaximumSize(new Dimension(width, height));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(theme.inputBorder, 1, (int) (height * 0.4)),
                BorderFactory.createEmptyBorder(3, 7, 3, 7)
        ));
    }

    public static JButton createOutlineButton(AppTheme theme, String text, int width, int height) {
        return createOutlineButton(theme, text, width, height, 8, new Font(Font.SANS_SERIF, Font.PLAIN, 15));
    }

    public static JButton createOutlineButton(
            AppTheme theme,
            String text,
            int width,
            int height,
            int cornerArc,
            Font font
    ) {
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
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerArc, cornerArc);
                    }
                    Color strokeColor = isEnabled() ? theme.buttonFg : theme.buttonFg.darker();
                    g2.setColor(strokeColor);
                    g2.setStroke(new BasicStroke(1.1f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, cornerArc, cornerArc);
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        btn.setFont(font);
        btn.setForeground(theme.buttonFg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMinimumSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        return btn;
    }

    public static void configureRoundedDialog(
            JDialog dialog,
            JPanel card,
            int cardArc,
            JButton submitButton,
            Runnable onWindowClosing
    ) {
        dialog.setContentPane(card);
        dialog.pack();
        if (dialog.getWidth() > 0 && dialog.getHeight() > 0) {
            dialog.setShape(new RoundRectangle2D.Float(0, 0, dialog.getWidth(), dialog.getHeight(), cardArc, cardArc));
        }

        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onWindowClosing != null) {
                    onWindowClosing.run();
                } else {
                    dialog.dispose();
                }
            }
        });

        if (submitButton != null) {
            dialog.getRootPane().setDefaultButton(submitButton);
        }
    }

    public static JPanel createCloseHeader(JDialog dialog, AppTheme theme) {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel closeBtn = new JLabel("×");
        closeBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        closeBtn.setForeground(theme.buttonFg);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        closeBtn.setToolTipText("Close");

        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(theme.buttonBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(theme.buttonFg);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
                System.exit(0);
            }
        });

        header.add(closeBtn);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, closeBtn.getPreferredSize().height));
        return header;
    }
}