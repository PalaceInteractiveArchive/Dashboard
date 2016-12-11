package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 8/22/16
 */
public class PacketFriendRequest extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String from;

    public PacketFriendRequest() {
        this(null, "");
    }

    public PacketFriendRequest(UUID uuid, String from) {
        this.id = PacketID.Dashboard.FRIENDREQUEST.getID();
        this.uuid = uuid;
        this.from = from;
    }

    public PacketFriendRequest fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.from = object.get("from").getAsString();

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("from", this.from);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}