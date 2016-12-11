package com.palacemc.dashboard.handlers;

import lombok.Getter;

import java.util.UUID;

public class Ban {
    @Getter private UUID uuid;

    @Getter private String name;
    @Getter private String reason;
    @Getter private String source;

    @Getter private boolean permanent;
    @Getter private long release;

    public Ban(UUID uuid, String name, boolean permanent, long release, String reason, String source) {
        this.uuid = uuid;
        this.name = name;
        this.permanent = permanent;
        this.release = release;
        this.reason = reason;
        this.source = source;
    }
}