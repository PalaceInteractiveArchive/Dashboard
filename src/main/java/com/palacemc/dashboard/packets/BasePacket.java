package com.palacemc.dashboard.packets;

import com.google.gson.JsonObject;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class BasePacket {
    @Getter protected int id = 0;

    public BasePacket fromJSON(JsonObject obj) {
        return this;
    }

    public JsonObject getJSON() {
        return null;
    }
}