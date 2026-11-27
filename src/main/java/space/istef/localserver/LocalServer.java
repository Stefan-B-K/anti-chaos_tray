package space.istef.localserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.istef.config.ConfigManager;
import space.istef.push.PushSubscription;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class LocalServer {
    private static final Logger logger = LoggerFactory.getLogger(LocalServer.class);
    private static final int PORT = 49152;
    private static final String SUBSCRIPTION_FILE = "browser_subscription.json";
    
    private final Javalin app;
    private final ConfigManager configManager;
    private final ObjectMapper objectMapper;
    private final String deviceId;

    public LocalServer(ConfigManager configManager, String deviceId) {
        this.configManager = configManager;
        this.objectMapper = new ObjectMapper();
        this.deviceId = deviceId;
        
        this.app = Javalin.create();
        
        setupRoutes();
        
        // Apply CORS to all routes
        app.before(ctx -> {
            List<String> allowedHosts = loadAllowedHosts();
            String origin = ctx.header("Origin");
            if (origin != null && allowedHosts.contains(origin)) {
                ctx.header("Access-Control-Allow-Origin", origin);
                ctx.header("Access-Control-Allow-Credentials", "true");
                ctx.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            }
        });
        
        // Handle preflight requests
        app.options("/*", ctx -> {
            List<String> allowedHosts = loadAllowedHosts();
            String origin = ctx.header("Origin");
            if (origin != null && allowedHosts.contains(origin)) {
                ctx.header("Access-Control-Allow-Origin", origin);
                ctx.header("Access-Control-Allow-Credentials", "true");
                ctx.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
                ctx.status(200);
            }
        });
    }

    private void setupRoutes() {
        // GET /device-id - Returns the device ID (no authentication required)
        app.get("/device-id", this::handleGetDeviceId);
        
        // POST /browser-subscription - Stores browser push subscription (no authentication required)
        app.post("/browser-subscription", this::handleBrowserSubscription);
    }

    private void handleGetDeviceId(Context ctx) {
        logger.info("GET /device_id request from: {}", ctx.ip());
        ctx.json(new DeviceIdResponse(deviceId));
    }

    private void handleBrowserSubscription(Context ctx) {
        try {
            logger.info("POST /browser-subscription request from: {}", ctx.ip());
            PushSubscription subscription = objectMapper.readValue(ctx.body(), PushSubscription.class);
            
            // Store the subscription in config
            configManager.saveJson(SUBSCRIPTION_FILE, subscription);
            logger.info("Browser subscription saved: endpoint={}", subscription.getEndpoint());
            
            ctx.status(200).result("Subscription saved");
        } catch (IOException e) {
            logger.error("Failed to save browser subscription", e);
            ctx.status(500).result("Failed to save subscription");
        }
    }

    public void start() {
        app.start(PORT);
        logger.info("Local server started on port {}", PORT);
    }

    public void stop() {
        app.stop();
        logger.info("Local server stopped");
    }

    private List<String> loadAllowedHosts() {
        Properties props = new Properties();
        
        try (InputStream in = LocalServer.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // application.properties is optional
        }
        
        try (InputStream in = LocalServer.class.getResourceAsStream("/local.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // local.properties is optional
        }
        
        String corsHosts = props.getProperty("app.local.cors.allowed-hosts", "http://localhost:5173,http://localhost:8088");
        List<String> hosts = new ArrayList<>();
        for (String host : corsHosts.split(",")) {
            hosts.add(host.trim());
        }
        
        logger.info("Loaded CORS allowed hosts: {}", hosts);
        return hosts;
    }

    private static class DeviceIdResponse {
        private final String deviceId;

        public DeviceIdResponse(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceId() {
            return deviceId;
        }
    }
}
