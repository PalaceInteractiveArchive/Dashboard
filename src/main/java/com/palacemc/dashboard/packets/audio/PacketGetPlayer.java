package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 6/15/15
 */
public class PacketGetPlayer extends BasePacket {
    private String token = "";

    public PacketGetPlayer() {
        this("");
    }

    public PacketGetPlayer(String token) {
        this.id = PacketID.GETPLAYER.getID();
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public PacketGetPlayer fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.token = obj.get("token").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("token", this.token);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}