package com.palacemc.dashboard.packets.bungee;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 11/25/16
 */
public class PacketBungeeID extends BasePacket {
    private UUID bungeeID;

    public PacketBungeeID() {
        this(null);
    }

    public PacketBungeeID(UUID bungeeID) {
        this.id = PacketID.Bungee.BUNGEEID.getID();
        this.bungeeID = bungeeID;
    }

    public UUID getBungeeID() {
        return bungeeID;
    }

    public PacketBungeeID fromJSON(JsonObject obj) {
        try {
            this.bungeeID = UUID.fromString(obj.get("bid").getAsString());
        } catch (Exception e) {
            this.bungeeID = null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("bid", this.bungeeID.toString());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}