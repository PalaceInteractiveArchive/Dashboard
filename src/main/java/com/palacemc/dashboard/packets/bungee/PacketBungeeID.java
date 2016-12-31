package com.palacemc.dashboard.packets.bungee;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 11/25/16
 */
public class PacketBungeeID extends BasePacket {
    @Getter private UUID bungeeId;

    public PacketBungeeID() {
        this(null);
    }

    public PacketBungeeID(UUID bungeeID) {
        this.id = PacketID.Bungee.BUNGEEID.getId();
        this.bungeeId = bungeeID;
    }

    public PacketBungeeID fromJSON(JsonObject object) {
        try {
            this.bungeeId = UUID.fromString(object.get("bid").getAsString());
        } catch (Exception e) {
            this.bungeeId = null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("bid", this.bungeeId.toString());
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}