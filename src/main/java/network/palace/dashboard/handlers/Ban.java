package network.palace.dashboard.handlers;

import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

public class Ban {
    private UUID uuid;
    @Getter private String name;
    @Getter private boolean permanent;
    @Getter private long created;
    @Getter private long expires;
    @Getter private String reason;
    @Getter private String source;

    public Ban(UUID uuid, String name, boolean permanent, long expires, String reason, String source) {
        this(uuid, name, permanent, System.currentTimeMillis(), expires, reason, source);
    }

    public Ban(UUID uuid, String name, boolean permanent, long created, long expires, String reason, String source) {
        this.uuid = uuid;
        this.name = name;
        this.permanent = permanent;
        this.created = created;
        this.expires = expires;
        this.reason = reason;
        this.source = source;
    }

    public Ban(UUID uuid, String name, Document structure) {
        this.uuid = uuid;
        this.name = name;
        if (structure == null) return;
        this.created = structure.getLong("created");
        this.expires = structure.getLong("expires");
        this.reason = structure.getString("reason");
        this.source = structure.getString("source");
    }

    public UUID getUniqueId() {
        return uuid;
    }
}
