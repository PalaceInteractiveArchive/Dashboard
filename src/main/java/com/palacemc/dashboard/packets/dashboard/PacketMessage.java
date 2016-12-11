package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 7/15/16
 */
public class PacketMessage extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String message;

    public PacketMessage() {
        this(null, "");
    }

    public PacketMessage(UUID uuid, String message) {
        this.id = PacketID.Dashboard.MESSAGE.getID();
        this.uuid = uuid;
        this.message = message;
    }

    public PacketMessage fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.message = object.get("message").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("message", this.message);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}