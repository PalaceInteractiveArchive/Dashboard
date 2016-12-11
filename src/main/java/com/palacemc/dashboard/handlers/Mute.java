package com.palacemc.dashboard.handlers;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Created by Marc on 7/16/16
 */
public class Mute {
    @Getter private UUID uuid;

    @Getter private String name;
    @Getter private String reason;
    @Getter private String source;

    @Getter @Setter private boolean muted;
    @Getter private long release;

    public Mute(UUID uuid, String name, boolean muted, long release, String reason, String source) {
        this.uuid = uuid;
        this.name = name;
        this.muted = muted;
        this.release = release;
        this.reason = reason;
        this.source = source;
    }
}