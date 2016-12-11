package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketSpeak extends BasePacket {
    @Getter private String voiceText = "";

    public PacketSpeak() {
        this("");
    }

    public PacketSpeak(String script) {
        this.id = PacketID.COMPUTER_SPEAK.getID();

        this.voiceText = script;
    }

    public PacketSpeak fromJSON(JsonObject object) {
        try {
            this.voiceText = object.get("voicetext").getAsString();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("voicetext", this.voiceText);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}