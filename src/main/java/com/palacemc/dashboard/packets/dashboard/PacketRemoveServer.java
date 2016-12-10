package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 8/25/16
 */
public class PacketRemoveServer extends BasePacket {
    private String name;

    public PacketRemoveServer() {
        this("");
    }

    public PacketRemoveServer(String name) {
        this.id = PacketID.Dashboard.REMOVESERVER.getID();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PacketRemoveServer fromJSON(JsonObject obj) {
        this.name = obj.get("name").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("name", this.name);
        } catch (Exception e) {
            return null;
        }

        return obj;
    }
}