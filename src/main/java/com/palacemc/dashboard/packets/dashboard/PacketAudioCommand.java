package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/2/16
 */
public class PacketAudioCommand extends BasePacket {
    @Getter private UUID uuid;
    @Getter private int auth;

    public PacketAudioCommand() {
        this(null, 0);
    }

    public PacketAudioCommand(UUID uuid, int auth) {
        this.id = PacketID.Dashboard.AUDIOCOMMAND.getID();
        this.uuid = uuid;
        this.auth = auth;
    }

    public PacketAudioCommand fromJSON(JsonObject object) {
        this.id = object.get("id").getAsInt();

        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.auth = object.get("auth").getAsInt();

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("auth", this.auth);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}
