package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/17/16
 */
public class PacketAudioConnect extends BasePacket {
    @Getter private UUID uuid;

    public PacketAudioConnect() {
        this(null);
    }

    public PacketAudioConnect(UUID uuid) {
        this.id = PacketID.Dashboard.AUDIOCONNECT.getID();
        this.uuid = uuid;
    }

    public PacketAudioConnect fromJSON(JsonObject object) {
        this.id = object.get("id").getAsInt();

        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}
