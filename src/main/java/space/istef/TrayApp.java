package space.istef;

import space.istef.UI.AppTheme;
import space.istef.UI.LoginDialog;
import space.istef.UI.TrayPopup;
import space.istef.auth.AuthService;
import space.istef.config.ConfigManager;
import space.istef.config.DeviceIdGenerator;
import space.istef.http.AuthenticatedHttpClient;
import space.istef.localserver.LocalServer;
import space.istef.push.BackendService;
import space.istef.push.PushSubscription;
import space.istef.push.PushSubscriptionService;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class TrayApp {

    private AppTheme.Mode currentMode;
    private TrayPopup popup;
    private LocalServer localServer;

    public static void main(String[] args) {
        TrayApp app = new TrayApp();
        // Add shutdown hook to stop local server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (app.localServer != null) {
                app.localServer.stop();
            }
        }));
        SwingUtilities.invokeLater(() -> app.start());
    }

    public void start() {
        this.currentMode = AppTheme.loadSavedMode();
        AppTheme activeTheme = AppTheme.of(currentMode);

        // Initialize services
        ConfigManager configManager = new ConfigManager();
        String baseUrl = loadBaseUrl();
        AuthService authService = new AuthService(configManager, baseUrl);
        AuthenticatedHttpClient httpClient = new AuthenticatedHttpClient(authService);
        BackendService backendService = new BackendService(httpClient, baseUrl);

        // Generate or retrieve device ID
        String deviceId = DeviceIdGenerator.generateDeviceId();

        // Check for existing authentication
        boolean loggedIn = false;
        if (authService.isAuthenticated()) {
            // Silent background auth - user already has valid token
            loggedIn = true;
        } else {
            // Show login dialog for new login or expired token
            LoginDialog loginDialog = new LoginDialog(activeTheme, authService);
            loggedIn = loginDialog.showLoginDialog();
        }

        if (!loggedIn) {
            System.exit(0);
            return;
        }

        // Register push subscription after successful login
        registerPushSubscription(backendService, deviceId, configManager);

        // Start local server for browser communication
        localServer = new LocalServer(configManager, deviceId);
        localServer.start();

        if (!SystemTray.isSupported()) {
            // Optional fallback window when SystemTray is unavailable
            return;
        }

        Image iconImage = loadTrayIconImage();
        TrayIcon trayIcon = new TrayIcon(iconImage, Messages.TRAY_TOOLTIP);
        trayIcon.setImageAutoSize(true);

        this.popup = new TrayPopup(
                activeTheme,
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

    private String loadBaseUrl() {
        Properties props = new Properties();
        
        // Load application.properties first
        try (InputStream in = TrayApp.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // application.properties is optional
        }
        
        // Load local.properties if it exists (overrides application.properties)
        try (InputStream in = TrayApp.class.getResourceAsStream("/local.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // local.properties is optional
        }
        
        String baseUrl = props.getProperty("app.base-url");
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalStateException("app.base-url not configured in application.properties or local.properties");
        }
        return baseUrl;
    }

    private void registerPushSubscription(BackendService backendService, String deviceId, ConfigManager configManager) {
        try {
            final String SUBSCRIPTION_FILE = "native_subscription.json";
            
            // Check if we have a locally stored subscription
            PushSubscription localSubscription = configManager.loadJson(SUBSCRIPTION_FILE, PushSubscription.class);
            
            // Validate with backend if we have a local subscription
            if (localSubscription != null) {
                boolean isValid = backendService.validateSubscription(deviceId);
                if (isValid) {
                    System.out.println("Existing subscription is valid, skipping re-subscription");
                    return;
                } else {
                    System.out.println("Existing subscription is invalid or expired, re-subscribing");
                }
            }
            
            // Generate new subscription and register with backend
            PushSubscriptionService pushService = new PushSubscriptionService();
            PushSubscription subscription = pushService.generateSubscription();
            
            boolean success = backendService.registerNativeSubscription(deviceId, subscription);
            if (success) {
                // Store subscription locally
                configManager.saveJson(SUBSCRIPTION_FILE, subscription);
                System.out.println("Push subscription registered successfully");
            } else {
                System.err.println("Failed to register push subscription");
            }
        } catch (Exception e) {
            System.err.println("Error during push subscription: " + e.getMessage());
            e.printStackTrace();
        }
    }
}