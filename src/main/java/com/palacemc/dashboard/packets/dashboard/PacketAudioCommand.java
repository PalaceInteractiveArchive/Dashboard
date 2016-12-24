package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/2/16
 */
public class PacketAudioCommand extends BasePacket {
    private UUID uuid;
    private String token;

    public PacketAudioCommand() {
        this(null, "");
    }

    public PacketAudioCommand(UUID uuid, String token) {
        this.id = PacketID.Dashboard.AUDIOCOMMAND.getID();
        this.uuid = uuid;
        this.token = token;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getToken() {
        return token;
    }

    public PacketAudioCommand fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.token = obj.get("token").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("token", this.token);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
