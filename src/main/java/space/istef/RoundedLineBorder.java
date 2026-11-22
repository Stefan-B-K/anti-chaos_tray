package space.istef;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.border.AbstractBorder;

public class RoundedLineBorder extends AbstractBorder {

    private final Color color;
    private final int thickness;
    private final int radius;

    public RoundedLineBorder(Color color, int thickness, int radius) {
        this.color = color;
        this.thickness = Math.max(1, thickness);
        this.radius = Math.max(0, radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness, thickness, thickness, thickness);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(thickness, thickness, thickness, thickness);
        return insets;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            float inset = thickness / 2.0f;
            g2.drawRoundRect(
                    Math.round(x + inset),
                    Math.round(y + inset),
                    Math.round(width - thickness),
                    Math.round(height - thickness),
                    radius,
                    radius
            );
        } finally {
            g2.dispose();
        }
    }
}
