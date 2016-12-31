package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketAudioSync extends BasePacket {
    private int audioId = 0;
    @Getter @Setter private double seconds = 1.0D;
    @Getter @Setter private double margin = 0.0D;

    public PacketAudioSync() {
        this(-1, 0.0F);
    }

    public PacketAudioSync(int audioId, float volume) {
        this.id = PacketID.AUDIO_SYNC.getID();

        this.audioId = audioId;
        this.seconds = volume;
    }

    public PacketAudioSync(int audioId, float volume, double margin) {
        this(audioId, volume);
        this.margin = margin;
    }

    public PacketAudioSync fromJSON(JsonObject object) {
        try {
            this.audioId = object.get("audioId").getAsInt();
            this.seconds = object.get("volume").getAsDouble();
            this.margin = object.get("margin").getAsDouble();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("audioId", this.audioId);
            object.addProperty("seconds", this.seconds);
            object.addProperty("margin", this.margin);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}