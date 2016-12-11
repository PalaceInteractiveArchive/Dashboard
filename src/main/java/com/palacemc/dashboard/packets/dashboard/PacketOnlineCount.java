package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 9/1/16
 */
public class PacketOnlineCount extends BasePacket {
    @Getter private int count;

    public PacketOnlineCount() {
        this(0);
    }

    public PacketOnlineCount(int count) {
        this.id = PacketID.Dashboard.ONLINECOUNT.getID();
        this.count = count;
    }

    public PacketOnlineCount fromJSON(JsonObject object) {
        this.count = object.get("count").getAsInt();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("count", this.count);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}