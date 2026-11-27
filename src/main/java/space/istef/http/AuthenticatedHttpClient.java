package space.istef.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.istef.auth.AuthService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthenticatedHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticatedHttpClient.class);
    
    private final HttpClient httpClient;
    private final AuthService authService;

    public AuthenticatedHttpClient(AuthService authService) {
        this.authService = authService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpResponse<String> post(String url, String jsonBody) throws IOException, InterruptedException {
        String token = authService.getJwtToken();
        if (token == null) {
            throw new IllegalStateException("No JWT token available. User must login first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        logger.debug("POST request to: {}", url);
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> get(String url) throws IOException, InterruptedException {
        String token = authService.getJwtToken();
        if (token == null) {
            throw new IllegalStateException("No JWT token available. User must login first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        logger.debug("GET request to: {}", url);
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> delete(String url, String jsonBody) throws IOException, InterruptedException {
        String token = authService.getJwtToken();
        if (token == null) {
            throw new IllegalStateException("No JWT token available. User must login first.");
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token);

        if (jsonBody != null && !jsonBody.isEmpty()) {
            requestBuilder.header("Content-Type", "application/json");
            requestBuilder.method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            requestBuilder.DELETE();
        }

        logger.debug("DELETE request to: {}", url);
        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
