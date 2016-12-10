package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 6/15/15
 */
public class PacketWarp extends BasePacket {
    private String warp = "";

    public PacketWarp() {
        this("");
    }

    public PacketWarp(String warp) {
        this.id = PacketID.INCOMING_WARP.getID();

        this.warp = warp;
    }

    public String getWarpName() {
        return this.warp;
    }

    public PacketWarp fromJSON(JsonObject obj) {
        try {
            this.warp = obj.get("warp").getAsString();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("warp", this.warp);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}