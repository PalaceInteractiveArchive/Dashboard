package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 6/15/15
 */
public class PacketStatusRequest extends BasePacket {

    public PacketStatusRequest() {
        this.id = PacketID.Dashboard.STATUSREQUEST.getID();
    }

    public PacketStatusRequest fromJSON(JsonObject obj) {
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}