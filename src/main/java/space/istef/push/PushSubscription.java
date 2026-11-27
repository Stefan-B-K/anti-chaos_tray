package space.istef.push;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PushSubscription {
    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("p256dh")
    private String p256dh;

    @JsonProperty("auth")
    private String auth;

    public PushSubscription() {
    }

    public PushSubscription(String endpoint, String p256dh, String auth) {
        this.endpoint = endpoint;
        this.p256dh = p256dh;
        this.auth = auth;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getP256dh() {
        return p256dh;
    }

    public void setP256dh(String p256dh) {
        this.p256dh = p256dh;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
