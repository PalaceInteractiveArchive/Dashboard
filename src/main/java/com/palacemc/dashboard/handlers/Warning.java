package com.palacemc.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
@AllArgsConstructor
public class Warning {
    @Getter private UUID uuid;
    @Getter private String name;
    @Getter private String message;
    @Getter private String response;
    @Getter private long expiration;
}