package space.istef.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.istef.config.ConfigManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String AUTH_FILE = "auth.json";
    
    private final ConfigManager configManager;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public AuthService(ConfigManager configManager, String baseUrl) {
        this.configManager = configManager;
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public boolean login(String email, String password) {
        try {
            String loginUrl = baseUrl + "/auth/login";
            LoginRequest loginRequest = new LoginRequest(email, password);
            String requestBody = objectMapper.writeValueAsString(loginRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(loginUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            logger.info("Attempting login to: {}", loginUrl);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AuthData authData = objectMapper.readValue(response.body(), AuthData.class);
                saveAuthData(authData);
                logger.info("Login successful, token saved");
                return true;
            } else {
                logger.error("Login failed with status code: {}, body: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Login request failed", e);
            return false;
        }
    }

    public String getJwtToken() {
        try {
            AuthData authData = configManager.loadJson(AUTH_FILE, AuthData.class);
            return authData != null ? authData.getToken() : null;
        } catch (IOException e) {
            logger.error("Failed to load auth data", e);
            return null;
        }
    }

    public String getRefreshToken() {
        try {
            AuthData authData = configManager.loadJson(AUTH_FILE, AuthData.class);
            return authData != null ? authData.getRefreshToken() : null;
        } catch (IOException e) {
            logger.error("Failed to load auth data", e);
            return null;
        }
    }

    public boolean isAuthenticated() {
        String token = getJwtToken();
        if (token == null) {
            return false;
        }
        // For MVP, just check if token exists and is not empty
        // The backend will validate it and return 401 if expired
        return !token.isEmpty();
    }

    public void logout() {
        try {
            configManager.deleteFile(AUTH_FILE);
            logger.info("Logged out, auth data deleted");
        } catch (IOException e) {
            logger.error("Failed to delete auth data", e);
        }
    }

    private void saveAuthData(AuthData authData) throws IOException {
        configManager.saveJson(AUTH_FILE, authData);
    }
}
