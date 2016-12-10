package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 9/1/16
 */
public class PacketOnlineCount extends BasePacket {
    private int count;

    public PacketOnlineCount() {
        this(0);
    }

    public PacketOnlineCount(int count) {
        this.id = PacketID.Dashboard.ONLINECOUNT.getID();
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public PacketOnlineCount fromJSON(JsonObject obj) {
        this.count = obj.get("count").getAsInt();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("count", this.count);
        } catch (Exception e) {
            return null;
        }

        return obj;
    }
}