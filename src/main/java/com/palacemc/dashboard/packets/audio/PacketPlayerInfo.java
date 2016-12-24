package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 6/15/15
 */
public class PacketPlayerInfo extends BasePacket {
    private UUID uuid;
    private String username;
    private String token;
    private String server;

    public PacketPlayerInfo() {
        this(null, "", "", "");
    }

    public PacketPlayerInfo(UUID uuid, String username, String token, String server) {
        this.id = PacketID.PLAYERINFO.getID();
        this.uuid = uuid;
        this.username = username;
        this.token = token;
        this.server = server;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getServer() {
        return server;
    }

    public PacketPlayerInfo fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.username = obj.get("username").getAsString();
        this.token = obj.get("token").getAsString();
        this.server = obj.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", uuid != null ? uuid.toString() : null);
            obj.addProperty("username", this.username);
            obj.addProperty("token", this.token);
            obj.addProperty("server", this.server);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}