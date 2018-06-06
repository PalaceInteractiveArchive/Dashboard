package network.palace.dashboard.handlers;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
public class ClickWarning {
    private UUID id;
    private String name;
    private String message;
    private String response;
    private long expiration;

    public ClickWarning(UUID id, String name, String message, String response, long expiration) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.response = response;
        this.expiration = expiration;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }

    public long getExpiration() {
        return expiration;
    }
}