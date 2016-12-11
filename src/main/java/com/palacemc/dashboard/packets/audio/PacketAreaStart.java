package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketAreaStart extends BasePacket {
    @Getter private int audioid = 0;
    @Getter private String name = "";
    @Getter private float volume = 1.0F;
    @Getter private int fadetime = 0;
    @Getter private boolean repeat = true;

    public PacketAreaStart() {
        this(-1, "", 1.0F, 0, true);
    }

    public PacketAreaStart(int audioid, String name, float volume, int fadetime, boolean repeat) {
        this.id = PacketID.AREA_START.getID();
        this.audioid = audioid;
        this.name = name;
        this.volume = volume;
        this.fadetime = fadetime;
        this.repeat = repeat;
    }

    public String getUsername() {
        return this.name;
    }

    public PacketAreaStart fromJSON(JsonObject object) {
        try {
            this.audioid = object.get("audioid").getAsInt();
            this.name = object.get("name").getAsString();
            this.volume = object.get("volume").getAsFloat();
            this.fadetime = object.get("fadetime").getAsInt();
            this.repeat = object.get("repeat").getAsBoolean();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("audioid", this.audioid);
            object.addProperty("name", this.name);
            object.addProperty("volume", this.volume);
            object.addProperty("fadetime", this.fadetime);
            object.addProperty("repeat", this.repeat);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}