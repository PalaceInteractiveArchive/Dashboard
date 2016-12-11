package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketKick extends BasePacket {
    @Getter private String message = "";
    @Getter private String reason = "";

    public PacketKick() {
        this("", "");
    }

    public PacketKick(String message) {
        this(message, "");
    }

    public PacketKick(String kickmessage, String reason) {
        this.id = PacketID.KICK.getID();

        this.message = kickmessage;
        this.reason = reason;
    }

    public PacketKick fromJSON(JsonObject object) {
        try {
            this.reason = object.get("reason").getAsString();
            this.message = object.get("message").getAsString();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("message", this.message);
            object.addProperty("reason", this.reason);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}