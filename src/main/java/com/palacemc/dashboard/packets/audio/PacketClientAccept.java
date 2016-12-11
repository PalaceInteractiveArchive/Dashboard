package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketClientAccept extends BasePacket {
    @Getter private String serverName = "";

    public PacketClientAccept(String serverName) {
        this.id = PacketID.CLIENT_ACCEPTED.getID();

        this.serverName = serverName;
    }

    public PacketClientAccept fromJSON(JsonObject object) {
        try {
            this.serverName = object.get("serverName").getAsString();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("serverName", this.serverName);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}