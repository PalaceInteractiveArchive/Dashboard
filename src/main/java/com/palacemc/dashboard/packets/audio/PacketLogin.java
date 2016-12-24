package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 6/15/15
 */
public class PacketLogin extends BasePacket {
    private int version = 7;
    private String token = "";

    public PacketLogin() {
        this(0, "");
    }

    public PacketLogin(int version, String token) {
        this.id = PacketID.LOGIN.getID();
        this.version = version;
        this.token = token;
    }

    public int getProtocolVersion() {
        return this.version;
    }

    public String getToken() {
        return token;
    }

    public PacketLogin fromJSON(JsonObject obj) {
        this.version = obj.get("version").getAsInt();
        this.token = obj.get("token").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("version", this.version);
            obj.addProperty("token", this.token);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
