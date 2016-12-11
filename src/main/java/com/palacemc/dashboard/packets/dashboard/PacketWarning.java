package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
public class PacketWarning extends BasePacket {
    @Getter private UUID warningId;
    @Getter private String username;
    @Getter private String message;
    @Getter private String action;

    public PacketWarning() {
        this(null, "", "", "");
    }

    public PacketWarning(UUID warningId, String username, String message, String action) {
        this.id = PacketID.Dashboard.WARNING.getID();
        this.warningId = warningId;
        this.username = username;
        this.message = message;
        this.action = action;
    }

    public PacketWarning fromJSON(JsonObject obj) {
        try {
            this.warningId = UUID.fromString(obj.get("warningid").getAsString());
        } catch (Exception e) {
            this.warningId = null;
        }

        this.username = obj.get("username").getAsString();
        this.message = obj.get("message").getAsString();
        this.action = obj.get("action").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("warningid", this.warningId.toString());
            obj.addProperty("username", this.username);
            obj.addProperty("message", this.message);
            obj.addProperty("action", this.action);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}