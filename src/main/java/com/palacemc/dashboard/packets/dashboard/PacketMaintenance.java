package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 9/12/16
 */
public class PacketMaintenance extends BasePacket {
    private boolean maintenance;

    public PacketMaintenance() {
        this(false);
    }

    public PacketMaintenance(boolean maintenance) {
        this.id = PacketID.Dashboard.MAINTENANCE.getID();
        this.maintenance = maintenance;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public PacketMaintenance fromJSON(JsonObject obj) {
        this.maintenance = obj.get("maintenance").getAsBoolean();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("maintenance", this.maintenance);
        } catch (Exception e) {
            return null;
        }

        return obj;
    }
}