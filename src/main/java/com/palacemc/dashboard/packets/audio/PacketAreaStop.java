package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketAreaStop extends BasePacket {
    @Getter private int audioId = 0;
    private int fadetime = 0;

    public PacketAreaStop() {
        this(-1, 0);
    }

    public PacketAreaStop(int audioId, int fadeTime) {
        this.id = PacketID.AREA_STOP.getID();

        this.audioId = audioId;
        this.fadetime = fadeTime;
    }

    public PacketAreaStop fromJSON(JsonObject object) {
        try {
            this.audioId = object.get("audioid").getAsInt();
            this.fadetime = object.get("fadetime").getAsInt();
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
            object.addProperty("fadetime", this.fadetime);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}