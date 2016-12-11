package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 7/15/16
 */
public class PacketServerSwitch extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String target;

    public PacketServerSwitch() {
        this(null, "");
    }

    public PacketServerSwitch(UUID uuid, String target) {
        this.id = PacketID.Dashboard.SERVERSWITCH.getID();
        this.uuid = uuid;
        this.target = target;
    }

    public PacketServerSwitch fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.target = object.get("target").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("target", this.target);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}