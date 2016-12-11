package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/17/16
 */
public class PacketSetPack extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String pack;

    public PacketSetPack() {
        this(null, "");
    }

    public PacketSetPack(UUID uuid, String pack) {
        this.id = PacketID.Dashboard.SETPACK.getID();
        this.uuid = uuid;
        this.pack = pack;
    }

    public PacketSetPack fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.pack = obj.get("pack").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("pack", this.pack);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}