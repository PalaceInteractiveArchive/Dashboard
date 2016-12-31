package com.palacemc.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Created by Marc on 7/16/16
 */
@AllArgsConstructor
public class Mute {
    @Getter private UUID uuid;

    @Getter private String name;
    @Getter private String reason;
    @Getter private String source;

    @Getter @Setter private boolean muted;
    @Getter private long release;
}