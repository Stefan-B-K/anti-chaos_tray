package space.istef.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.istef.http.AuthenticatedHttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BackendService {
    private static final Logger logger = LoggerFactory.getLogger(BackendService.class);
    
    private final AuthenticatedHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public BackendService(AuthenticatedHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
    }

    public boolean validateSubscription(String deviceId) {
        try {
            String encodedDeviceId = URLEncoder.encode(deviceId, StandardCharsets.UTF_8);
            String url = baseUrl + "/api/push/native/validate?deviceId=" + encodedDeviceId;

            logger.info("Validating subscription for device: {}", deviceId);
            var response = httpClient.get(url);

            if (response.statusCode() == 200) {
                SubscriptionValidationResponse validationResponse = 
                    objectMapper.readValue(response.body(), SubscriptionValidationResponse.class);
                logger.info("Subscription validation result: valid={}, message={}", 
                    validationResponse.isValid(), validationResponse.getMessage());
                return validationResponse.isValid();
            } else {
                logger.error("Failed to validate subscription. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to validate subscription", e);
            return false;
        }
    }

    public boolean registerNativeSubscription(String deviceId, PushSubscription subscription) {
        try {
            String url = baseUrl + "/api/push/native/subscribe";
            NativeSubscriptionRequest request = new NativeSubscriptionRequest(
                deviceId,
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth()
            );
            String requestBody = objectMapper.writeValueAsString(request);

            logger.info("Registering native subscription to: {}", url);
            var response = httpClient.post(url, requestBody);

            if (response.statusCode() == 200) {
                logger.info("Native subscription registered successfully");
                return true;
            } else {
                logger.error("Failed to register native subscription. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to register native subscription", e);
            return false;
        }
    }

    public boolean unregisterNativeSubscription(String deviceId) {
        try {
            String url = baseUrl + "/api/push/native/unsubscribe";
            String requestBody = objectMapper.writeValueAsString(new DeviceIdRequest(deviceId));

            logger.info("Unregistering native subscription from: {}", url);
            var response = httpClient.delete(url, requestBody);

            if (response.statusCode() == 200) {
                logger.info("Native subscription unregistered successfully");
                return true;
            } else {
                logger.error("Failed to unregister native subscription. Status: {}, Body: {}", 
                    response.statusCode(), response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to unregister native subscription", e);
            return false;
        }
    }

    private static class DeviceIdRequest {
        @SuppressWarnings("unused")
        private final String deviceId;

        public DeviceIdRequest(String deviceId) {
            this.deviceId = deviceId;
        }
    }
}
