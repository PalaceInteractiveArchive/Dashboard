package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 6/15/15
 */
public class PacketPlayerInfo extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String username;
    @Getter private int auth;
    @Getter private String server;

    public PacketPlayerInfo() {
        this(null, "", 0, "");
    }

    public PacketPlayerInfo(UUID uuid, String username, int auth, String server) {
        this.id = PacketID.PLAYERINFO.getID();
        this.uuid = uuid;
        this.username = username;
        this.auth = auth;
        this.server = server;
    }

    public PacketPlayerInfo fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.username = object.get("username").getAsString();
        this.auth = object.get("auth").getAsInt();
        this.server = object.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", uuid != null ? uuid.toString() : null);
            object.addProperty("username", this.username);
            object.addProperty("auth", this.auth);
            object.addProperty("server", this.server);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}