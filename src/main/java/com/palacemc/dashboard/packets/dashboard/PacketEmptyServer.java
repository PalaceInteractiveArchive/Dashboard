package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 9/18/16
 */
public class PacketEmptyServer extends BasePacket {
    @Getter private String server;

    public PacketEmptyServer() {
        this("");
    }

    public PacketEmptyServer(String server) {
        this.id = PacketID.Dashboard.EMPTYSERVER.getID();
        this.server = server;
    }

    public PacketEmptyServer fromJSON(JsonObject object) {
        this.server = object.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("server", this.server);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}