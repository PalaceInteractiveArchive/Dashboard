package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 8/25/16
 */
public class PacketTargetLobby extends BasePacket {
    @Getter private String server;

    public PacketTargetLobby() {
        this("");
    }

    public PacketTargetLobby(String server) {
        this.id = PacketID.Dashboard.TARGETLOBBY.getID();
        this.server = server;
    }

    public PacketTargetLobby fromJSON(JsonObject obj) {
        this.server = obj.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("server", this.server);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}