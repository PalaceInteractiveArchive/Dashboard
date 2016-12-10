package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
public class PacketMyMCMagicRegister extends BasePacket {
    private UUID uuid;
    private int pin;

    public PacketMyMCMagicRegister() {
        this(null, 0);
    }

    public PacketMyMCMagicRegister(UUID uuid, int pin) {
        this.id = PacketID.Dashboard.MYMCMAGICREGISTER.getID();
        this.uuid = uuid;
        this.pin = pin;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public int getPin() {
        return pin;
    }

    public PacketMyMCMagicRegister fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.pin = obj.get("pin").getAsInt();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("pin", this.pin);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}