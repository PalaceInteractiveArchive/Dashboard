package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/22/16
 */
public class PacketLink extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String url;
    @Getter private String name;
    @Getter private ChatColor color;
    @Getter private boolean bold;
    @Getter private boolean spacing;

    public PacketLink() {
        this(null, "", "", ChatColor.YELLOW, true);
    }

    public PacketLink(UUID uuid, String url, String name, ChatColor color, boolean bold) {
        this(uuid, url, name, color, bold, true);
    }

    public PacketLink(UUID uuid, String url, String name, ChatColor color, boolean bold, boolean spacing) {
        this.id = PacketID.Dashboard.LINK.getID();
        this.uuid = uuid;
        this.url = url;
        this.name = name;
        this.color = color;
        this.bold = bold;
        this.spacing = spacing;
    }

    public PacketLink fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.url = object.get("url").getAsString();
        this.name = object.get("name").getAsString();
        this.color = ChatColor.valueOf(object.get("color").getAsString());
        this.bold = object.get("bold").getAsBoolean();
        this.spacing = object.get("spacing").getAsBoolean();

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("url", this.url);
            object.addProperty("name", this.name);
            object.addProperty("color", this.color.name());
            object.addProperty("bold", this.bold);
            object.addProperty("spacing", this.spacing);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}