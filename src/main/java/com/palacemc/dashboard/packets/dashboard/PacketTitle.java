package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/28/16
 */
public class PacketTitle extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String title;
    @Getter private String subtitle;
    @Getter private int fadeIn;
    @Getter private int stay;
    @Getter private int fadeOut;

    public PacketTitle() {
        this(null, "", "", 0, 0, 0);
    }

    public PacketTitle(UUID uuid, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.id = PacketID.Dashboard.TITLE.getID();
        this.uuid = uuid;
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public PacketTitle fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.title = obj.get("title").getAsString();
        this.subtitle = obj.get("subtitle").getAsString();
        this.fadeIn = obj.get("fadeIn").getAsInt();
        this.stay = obj.get("stay").getAsInt();
        this.fadeOut = obj.get("fadeOut").getAsInt();
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        obj.addProperty("id", this.id);
        obj.addProperty("uuid", this.uuid.toString());
        obj.addProperty("title", this.title);
        obj.addProperty("subtitle", this.subtitle);
        obj.addProperty("fadeIn", this.fadeIn);
        obj.addProperty("stay", this.stay);
        obj.addProperty("fadeOut", this.fadeOut);
        return obj;
    }
}