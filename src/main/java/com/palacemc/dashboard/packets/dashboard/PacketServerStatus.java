package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketServerStatus extends BasePacket {
    @Getter private int onlineCount;
    @Getter private JsonArray serverStatus;

    public PacketServerStatus() {
        this(0, null);
    }

    public PacketServerStatus(int onlineCount, JsonArray serverStatus) {
        this.id = PacketID.Dashboard.SERVERSTATUS.getID();
        this.onlineCount = onlineCount;
        this.serverStatus = serverStatus;
    }

    public PacketServerStatus fromJSON(JsonObject object) {
        this.onlineCount = object.get("onlineCount").getAsInt();
        this.serverStatus = object.get("serverStatus").getAsJsonArray();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("onlineCount", this.onlineCount);
            object.add("serverStatus", this.serverStatus);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}