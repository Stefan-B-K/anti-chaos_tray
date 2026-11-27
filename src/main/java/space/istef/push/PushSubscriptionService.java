package space.istef.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Properties;

public class PushSubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(PushSubscriptionService.class);
    
    private final String publicKey;
    private final String privateKey;
    private final String subject;

    public PushSubscriptionService() {
        Properties props = loadVapidKeys();
        this.publicKey = props.getProperty("app.push.vapid.public-key");
        this.privateKey = props.getProperty("app.push.vapid.private-key");
        this.subject = props.getProperty("app.push.vapid.subject");
        
        if (publicKey == null || privateKey == null || subject == null) {
            throw new IllegalStateException("VAPID keys not configured in local.properties");
        }
    }

    public PushSubscription generateSubscription() {
        try {
            // Generate a new subscription
            KeyPair keyPair = generateKeyPair();
            
            // Extract raw key bytes and base64 encode
            ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
            ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
            
            String p256dh = Base64.getUrlEncoder().withoutPadding().encodeToString(ecPublicKey.getEncoded());
            String auth = Base64.getUrlEncoder().withoutPadding().encodeToString(ecPrivateKey.getEncoded());
            
            // Generate endpoint
            String endpoint = "https://fcm.googleapis.com/fcm/send/" + generateEndpoint();
            
            PushSubscription pushSub = new PushSubscription(endpoint, p256dh, auth);
            
            logger.info("Generated push subscription with endpoint: {}", endpoint);
            return pushSub;
        } catch (Exception e) {
            logger.error("Failed to generate push subscription", e);
            throw new RuntimeException("Failed to generate push subscription", e);
        }
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException, 
            InvalidAlgorithmParameterException {
        ECGenParameterSpec parameterSpec = new ECGenParameterSpec("secp256r1");
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(parameterSpec);
        return keyPairGenerator.generateKeyPair();
    }

    private String generateEndpoint() {
        // Generate a random endpoint identifier
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Properties loadVapidKeys() {
        Properties props = new Properties();
        
        try (InputStream in = PushSubscriptionService.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // application.properties is optional
        }
        
        try (InputStream in = PushSubscriptionService.class.getResourceAsStream("/local.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // local.properties is optional
        }
        
        return props;
    }
}
