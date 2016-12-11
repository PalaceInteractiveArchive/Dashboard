package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 7/14/16
 */
public class PacketPlayerJoin extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String username;
    @Getter private String server;
    @Getter private String address;

    public PacketPlayerJoin() {
        this(null, "", "", "");
    }

    public PacketPlayerJoin(UUID uuid, String username, String server, String address) {
        this.id = PacketID.Dashboard.PLAYERJOIN.getID();
        this.uuid = uuid;
        this.username = username;
        this.server = server;
        this.address = address;
    }

    public PacketPlayerJoin fromJSON(JsonObject object) {
        this.id = object.get("id").getAsInt();

        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.username = object.get("username").getAsString();
        this.server = object.get("server").getAsString();
        this.address = object.get("address").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("username", this.username);
            object.addProperty("server", this.server);
            object.addProperty("address", this.address);
        } catch (Exception e) {
            return null;
        }
        return object;
    }

    public String getRank() {
        return null;
    }
}