package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 8/22/16
 */
public class PacketBSeenCommand extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String username;
    @Getter private String address;
    @Getter private String server;
    @Getter private boolean online;

    public PacketBSeenCommand() {
        this(null, "", "", "", false);
    }

    public PacketBSeenCommand(UUID uuid, String username, String address, String server, boolean online) {
        this.id = PacketID.Dashboard.BSEENCOMMAND.getID();
        this.uuid = uuid;
        this.username = username;
        this.address = address;
        this.server = server;
        this.online = online;
    }

    public PacketBSeenCommand fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.username = object.get("username").getAsString();
        this.address = object.get("address").getAsString();
        this.server = object.get("server").getAsString();
        this.online = object.get("online").getAsBoolean();

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("username", this.username);
            object.addProperty("address", this.address);
            object.addProperty("server", this.server);
            object.addProperty("online", this.online);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}