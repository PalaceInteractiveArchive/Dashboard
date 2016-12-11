package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/17/16
 */
public class PacketGetPack extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String pack;

    public PacketGetPack() {
        this(null, "");
    }

    public PacketGetPack(UUID uuid, String pack) {
        this.id = PacketID.Dashboard.GETPACK.getID();
        this.uuid = uuid;
        this.pack = pack;
    }

    public PacketGetPack fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.pack = object.get("pack").getAsString();

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