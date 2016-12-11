package com.palacemc.dashboard.packets.park;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 9/18/16
 */
public class PacketRefreshHotels extends BasePacket {
    @Getter private String server;

    public PacketRefreshHotels() {
        this("");
    }

    public PacketRefreshHotels(String server) {
        this.id = PacketID.Park.REFRESHHOTELS.getId();
        this.server = server;
    }

    public PacketRefreshHotels fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.server = obj.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        obj.addProperty("id", this.id);
        obj.addProperty("server", this.server);
        return obj;
    }
}