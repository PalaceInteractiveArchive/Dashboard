package com.palacemc.dashboard.handlers;

import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
public class Warning {
    @Getter private UUID id;
    @Getter private String name;
    @Getter private String message;
    @Getter private String response;
    @Getter private long expiration;

    public Warning(UUID id, String name, String message, String response, long expiration) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.response = response;
        this.expiration = expiration;
    }
}