package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 5/24/16
 */
public class PacketContainer extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String container;

    public PacketContainer() {
    }

    public PacketContainer(UUID uuid, String container) {
        this.id = PacketID.CONTAINER.getID();
        this.uuid = uuid;
        this.container = container;
    }

    public PacketContainer fromJSON(JsonObject object) {
        this.id = object.get("id").getAsInt();

        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.container = object.get("container").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", uuid != null ? uuid.toString() : null);
            object.addProperty("container", this.container);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}