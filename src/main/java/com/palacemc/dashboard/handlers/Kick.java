package com.palacemc.dashboard.handlers;

import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 8/25/16
 */
public class Kick {
    @Getter private UUID uuid;
    @Getter private String reason;
    @Getter private String source;

    public Kick(UUID uuid, String reason, String source) {
        this.uuid = uuid;
        this.reason = reason;
        this.source = source;
    }
}