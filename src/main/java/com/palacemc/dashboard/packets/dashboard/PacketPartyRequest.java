package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 8/22/16
 */
public class PacketPartyRequest extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String from;

    public PacketPartyRequest() {
        this(null, "");
    }

    public PacketPartyRequest(UUID uuid, String from) {
        this.id = PacketID.Dashboard.PARTYREQUEST.getID();
        this.uuid = uuid;
        this.from = from;
    }

    public PacketPartyRequest fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.from = obj.get("from").getAsString();
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