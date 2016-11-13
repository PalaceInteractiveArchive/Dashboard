package com.palacemc.dashboard.packets.park;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/18/16
 */
public class PacketInventoryStatus extends BasePacket {
    private UUID uuid;
    private int status;

    /**
     * 0 = uploaded (sent from previous server)
     * 1 = download (sent from Dashboard to new server)
     */
    public PacketInventoryStatus() {
        this(null, 0);
    }

    public PacketInventoryStatus(UUID uuid, int status) {
        this.id = PacketID.Park.INVENTORYSTATUS.getID();
        this.uuid = uuid;
        this.status = status;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int getStatus() {
        return status;
    }

    public PacketInventoryStatus fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.status = obj.get("status").getAsInt();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("uuid", this.uuid.toString());
        obj.addProperty("status", this.status);
        return obj;
    }
}