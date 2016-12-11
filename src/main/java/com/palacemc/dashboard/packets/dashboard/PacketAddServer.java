package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 8/25/16
 */
public class PacketAddServer extends BasePacket {
    @Getter private String name;
    @Getter private String address;
    @Getter private int port;

    public PacketAddServer() {
        this("", "", 0);
    }

    public PacketAddServer(String name, String address, int port) {
        this.id = PacketID.Dashboard.ADDSERVER.getID();
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public PacketAddServer fromJSON(JsonObject object) {
        this.name = object.get("name").getAsString();
        this.address = object.get("address").getAsString();
        this.port = object.get("port").getAsInt();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("name", this.name);
            object.addProperty("address", this.address);
            object.addProperty("port", this.port);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}