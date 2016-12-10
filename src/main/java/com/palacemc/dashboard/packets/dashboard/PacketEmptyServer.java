package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 9/18/16
 */
public class PacketEmptyServer extends BasePacket {
    private String server;

    public PacketEmptyServer() {
        this("");
    }

    public PacketEmptyServer(String server) {
        this.id = PacketID.Dashboard.EMPTYSERVER.getID();
        this.server = server;
    }

    public String getServer() {
        return server;
    }

    public PacketEmptyServer fromJSON(JsonObject obj) {
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