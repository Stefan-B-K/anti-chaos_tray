package space.istef;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.io.InputStream;

public class TrayApp {

    public static void main(String[] args) {
        // Must be set before AWT initializes on macOS to avoid a Dock icon
        System.setProperty("apple.awt.UIElement", "true");

        SwingUtilities.invokeLater(() -> {
            if (!SystemTray.isSupported()) {
                System.err.println("SystemTray is not supported on this platform/desktop environment.");
                return;
            }

            try {
                Image iconImage = loadTrayIcon(Messages.ICON_RESOURCE_PATH);
                SystemTray tray = SystemTray.getSystemTray();

                PopupMenu popup = new PopupMenu();
                MenuItem exitItem = new MenuItem(Messages.MENU_EXIT);
                popup.add(exitItem);

                TrayIcon trayIcon = new TrayIcon(iconImage, Messages.TRAY_TOOLTIP, popup);
                trayIcon.setImageAutoSize(true);

                exitItem.addActionListener(e -> {
                    tray.remove(trayIcon);
                    System.exit(0);
                });

                tray.add(trayIcon);
            } catch (IOException e) {
                System.err.println("Failed to load tray icon: " + e.getMessage());
            } catch (AWTException e) {
                System.err.println("Failed to add TrayIcon: " + e.getMessage());
            }
        });
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