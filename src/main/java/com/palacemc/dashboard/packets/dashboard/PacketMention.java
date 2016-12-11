package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/17/16
 */
public class PacketMention extends BasePacket {
    @Getter private UUID uuid;

    public PacketMention() {
        this(null);
    }

    public PacketMention(UUID uuid) {
        this.id = PacketID.Dashboard.MENTION.getID();
        this.uuid = uuid;
    }

    public PacketMention fromJSON(JsonObject object) {
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