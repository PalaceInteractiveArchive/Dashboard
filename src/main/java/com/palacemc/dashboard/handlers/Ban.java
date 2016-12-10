package com.palacemc.dashboard.handlers;

import java.util.UUID;

public class Ban {
    private UUID uuid;

    private String name;
    private String reason;
    private String source;

    private boolean permanent;
    private long release;

    public Ban(UUID uuid, String name, boolean permanent, long release, String reason, String source) {
        this.uuid = uuid;
        this.name = name;
        this.permanent = permanent;
        this.release = release;
        this.reason = reason;
        this.source = source;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public long getRelease() {
        return release;
    }

    public String getReason() {
        return reason;
    }

    public String getSource() {
        return source;
    }
}