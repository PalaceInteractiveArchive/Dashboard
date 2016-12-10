package com.palacemc.dashboard.packets.park;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/18/16
 */
public class PacketWarp extends BasePacket {
    private UUID uuid;
    private String warp;
    private String server;

    public PacketWarp() {
        this(null, "", "");
    }

    public PacketWarp(UUID uuid, String warp, String server) {
        this.id = PacketID.Park.WARP.getID();
        this.uuid = uuid;
        this.warp = warp;
        this.server = server;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getWarp() {
        return warp;
    }

    public String getServer() {
        return server;
    }

    public PacketWarp fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();

        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.warp = obj.get("warp").getAsString();
        this.server = obj.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        obj.addProperty("id", this.id);
        obj.addProperty("uuid", this.uuid.toString());
        obj.addProperty("warp", this.warp);
        obj.addProperty("server", this.server);
        return obj;
    }
}