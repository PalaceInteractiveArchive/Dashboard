package com.palacemc.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 8/25/16
 */
@AllArgsConstructor
public class Kick {
    @Getter private UUID uuid;
    @Getter private String reason;
    @Getter private String source;
}