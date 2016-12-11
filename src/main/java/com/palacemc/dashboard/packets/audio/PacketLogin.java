package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketLogin extends BasePacket {
    @Getter private int version = 7;
    @Getter private String playername = "";
    @Getter private String auth = "";

    public PacketLogin() {
        this(0, "", "");
    }

    public PacketLogin(int version, String playername, String auth) {
        this.id = PacketID.LOGIN.getID();

        this.version = version;
        this.playername = playername;
        this.auth = auth;
    }

    public PacketLogin fromJSON(JsonObject object) {
        this.version = object.get("version").getAsInt();
        this.playername = object.get("playername").getAsString();
        this.auth = object.get("auth").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("version", this.version);
            object.addProperty("playername", this.playername);
            object.addProperty("auth", this.auth);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}