package network.palace.dashboard.handlers;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

/**
 * Created by Marc on 7/16/16
 */
public class Mute {
    private UUID uuid;
    @Getter private String name;
    @Getter @Setter private boolean muted;
    @Getter @Setter private long created;
    @Getter @Setter private long expires;
    @Getter @Setter private String reason;
    @Getter @Setter private String source;

    public Mute(UUID uuid, String name, boolean muted, long created, long expires, String reason, String source) {
        this.uuid = uuid;
        this.name = name;
        this.muted = muted;
        this.created = created;
        this.expires = expires;
        this.reason = reason;
        this.source = source;
    }

    public Mute(UUID uuid, String name, Document structure) {
        this.uuid = uuid;
        this.name = name;
        this.muted = structure != null;
        if (!muted) return;
        this.created = structure.getLong("created");
        this.expires = structure.getLong("expires");
        this.reason = structure.getString("reason");
        this.source = structure.getString("source");
    }

    public UUID getUniqueId() {
        return uuid;
    }
}