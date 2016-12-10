package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/17/16
 */
public class PacketAudioConnect extends BasePacket {
    private UUID uuid;

    public PacketAudioConnect() {
        this(null);
    }

    public PacketAudioConnect(UUID uuid) {
        this.id = PacketID.Dashboard.AUDIOCONNECT.getID();
        this.uuid = uuid;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PacketAudioConnect fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();

        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
        } catch (Exception e) {
            return null;
        }

        return obj;
    }
}
