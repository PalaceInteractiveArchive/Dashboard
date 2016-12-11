package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketNotification extends BasePacket {
    @Getter private String text = "";
    @Getter private String body = "";
    @Getter private String icon = "";

    public PacketNotification() {
        this("", "", "");
    }

    public PacketNotification(String text, String body, String icon) {
        this.id = PacketID.NOTIFICATION.getID();

        this.text = text;
        this.body = body;
        this.icon = icon;
    }

    public PacketNotification fromJSON(JsonObject object) {
        try {
            this.text = object.get("text").getAsString();
            this.body = object.get("body").getAsString();
            this.icon = object.get("icon").getAsString();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("text", this.text);
            object.addProperty("body", this.body);
            object.addProperty("icon", this.icon);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}