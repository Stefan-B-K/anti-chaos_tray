package space.istef;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class TrayApp {

    private AppTheme.Mode currentMode;
    private TrayPopup popup;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrayApp().start());
    }

    public void start() {
        this.currentMode = AppTheme.loadSavedMode();

        if (!SystemTray.isSupported()) {
            // Optional fallback window when SystemTray is unavailable
            return;
        }

        Image iconImage = loadTrayIconImage();
        TrayIcon trayIcon = new TrayIcon(iconImage, Messages.TRAY_TOOLTIP);
        trayIcon.setImageAutoSize(true);

        this.popup = new TrayPopup(
                AppTheme.of(currentMode),
                visible -> trayIcon.setToolTip(visible ? null : Messages.TRAY_TOOLTIP)
        );

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                popup.showNearPointer();
            }
        });

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException ex) {
            System.err.println("Failed to add tray icon: " + ex);
        }
    }

    public AppTheme.Mode getCurrentMode() {
        return currentMode;
    }

    public void switchTheme(AppTheme.Mode newMode) {
        if (newMode == null || newMode == this.currentMode) {
            return;
        }
        this.currentMode = newMode;
        AppTheme.saveMode(newMode);
        if (popup != null) {
            popup.applyTheme(AppTheme.of(newMode));
        }
    }

    private Image loadTrayIconImage() {
        String iconPath = Messages.ICON_RESOURCE_PATH;
        try {
            return loadTrayIcon(iconPath);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load tray icon from " + iconPath, ex);
        }
    }

    private static Image loadTrayIcon(String resourcePath) throws IOException {
        try (InputStream in = TrayApp.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Icon resource not found on classpath: " + resourcePath);
            }
            Image image = ImageIO.read(in);
            if (image == null) {
                throw new IOException("Unsupported or corrupted image format: " + resourcePath);
            }
            return image;
        }
    }
}