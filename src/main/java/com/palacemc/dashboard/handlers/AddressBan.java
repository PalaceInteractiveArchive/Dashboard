package com.palacemc.dashboard.handlers;

import lombok.Getter;

public class AddressBan {
    @Getter private String address;
    @Getter private String reason;
    @Getter private String source;

    public AddressBan(String address, String reason, String source) {
        this.address = address;
        this.reason = reason;
        this.source = source;
    }
}