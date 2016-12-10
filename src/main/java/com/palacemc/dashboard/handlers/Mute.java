package com.palacemc.dashboard.handlers;

import java.util.UUID;

/**
 * Created by Marc on 7/16/16
 */
public class Mute {
    private UUID uuid;

    private String name;
    private String reason;
    private String source;

    private boolean muted;
    private long release;

    public Mute(UUID uuid, String name, boolean muted, long release, String reason, String source) {
        this.uuid = uuid;
        this.name = name;
        this.muted = muted;
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

    public boolean isMuted() {
        return muted;
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

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void setRelease(long release) {
        this.release = release;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}