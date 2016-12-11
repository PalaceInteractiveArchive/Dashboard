package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
public class PacketMyMCMagicRegister extends BasePacket {
    @Getter private UUID uuid;
    @Getter private int pin;

    public PacketMyMCMagicRegister() {
        this(null, 0);
    }

    public PacketMyMCMagicRegister(UUID uuid, int pin) {
        this.id = PacketID.Dashboard.MYMCMAGICREGISTER.getID();
        this.uuid = uuid;
        this.pin = pin;
    }

    public PacketMyMCMagicRegister fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.pin = object.get("pin").getAsInt();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("pin", this.pin);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}