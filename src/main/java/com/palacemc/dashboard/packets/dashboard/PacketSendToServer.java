package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 8/22/16
 */
public class PacketSendToServer extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String server;

    public PacketSendToServer() {
        this(null, "");
    }

    public PacketSendToServer(UUID uuid, String server) {
        this.id = PacketID.Dashboard.SENDTOSERVER.getID();
        this.uuid = uuid;
        this.server = server;
    }

    public PacketSendToServer fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.server = object.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("server", this.server);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}
