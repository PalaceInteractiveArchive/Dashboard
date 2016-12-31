package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketPlayOnceGlobal extends BasePacket {
    @Getter private int audioId = 0;
    @Getter private String name = "";
    @Getter private float volume = 1.0F;

    public PacketPlayOnceGlobal() {
        this(-1, "", 1.0F);
    }

    public PacketPlayOnceGlobal(int audioId, String name) {
        this(audioId, name, 1.0F);
    }

    public PacketPlayOnceGlobal(int audioId, String name, float volume) {
        this.id = PacketID.GLOBAL_PLAY_ONCE.getID();

        this.audioId = audioId;
        this.name = name;
        this.volume = volume;
    }

    public PacketPlayOnceGlobal fromJSON(JsonObject object) {
        try {
            this.audioId = object.get("audioid").getAsInt();
            this.name = object.get("name").getAsString();
            this.volume = object.get("volume").getAsFloat();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("audioid", this.audioId);
            object.addProperty("name", this.name);
            object.addProperty("volume", this.volume);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}