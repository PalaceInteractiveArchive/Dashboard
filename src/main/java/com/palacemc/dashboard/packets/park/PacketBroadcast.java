package com.palacemc.dashboard.packets.park;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 9/18/16
 */
public class PacketBroadcast extends BasePacket {
    private String message;
    private String source;

    public PacketBroadcast() {
        this("", "");
    }

    public PacketBroadcast(String message, String source) {
        this.id = PacketID.Park.BROADCAST.getID();
        this.message = message;
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    public PacketBroadcast fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.message = obj.get("message").getAsString();
        this.source = obj.get("source").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("message", this.message);
        obj.addProperty("source", this.source);
        return obj;
    }
}