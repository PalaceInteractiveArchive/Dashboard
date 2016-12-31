package com.palacemc.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public class Ban {
    @Getter private UUID uuid;

    @Getter private String name;

    @Getter private boolean permanent;
    @Getter private long release;

    @Getter private String reason;
    @Getter private String source;
}