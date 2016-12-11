package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 9/12/16
 */
public class PacketMaintenance extends BasePacket {
    @Getter private boolean maintenance;

    public PacketMaintenance() {
        this(false);
    }

    public PacketMaintenance(boolean maintenance) {
        this.id = PacketID.Dashboard.MAINTENANCE.getID();
        this.maintenance = maintenance;
    }

    public PacketMaintenance fromJSON(JsonObject object) {
        this.maintenance = object.get("maintenance").getAsBoolean();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("maintenance", this.maintenance);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}