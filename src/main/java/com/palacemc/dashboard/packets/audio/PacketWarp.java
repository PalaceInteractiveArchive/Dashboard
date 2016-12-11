package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketWarp extends BasePacket {
    @Getter private String warp = "";

    public PacketWarp() {
        this("");
    }

    public PacketWarp(String warp) {
        this.id = PacketID.INCOMING_WARP.getID();

        this.warp = warp;
    }

    public PacketWarp fromJSON(JsonObject object) {
        try {
            this.warp = object.get("warp").getAsString();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("warp", this.warp);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}