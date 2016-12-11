package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 7/14/16
 */
public class PacketPlayerDisconnect extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String reason;

    public PacketPlayerDisconnect() {
        this(null, "");
    }

    public PacketPlayerDisconnect(UUID uuid, String reason) {
        this.id = PacketID.Dashboard.PLAYERDISCONNECT.getID();
        this.uuid = uuid;
        this.reason = reason;
    }

    public PacketPlayerDisconnect fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.reason = object.get("reason").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("reason", this.reason);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}