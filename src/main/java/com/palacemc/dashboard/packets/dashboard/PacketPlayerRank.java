package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.UUID;

public class PacketPlayerRank extends BasePacket {
    @Getter private UUID uuid;
    @Getter private Rank rank;

    public PacketPlayerRank() {
        this(null, Rank.SETTLER);
    }

    public PacketPlayerRank(UUID uuid, Rank rank) {
        this.id = PacketID.Dashboard.PLAYERRANK.getID();
        this.uuid = uuid;
        this.rank = rank;
    }

    public PacketPlayerRank fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.rank = Rank.fromString(object.get("rank").getAsString());
        return this;
    }

    public JsonObject getJSON() {

        JsonObject object = new JsonObject();
        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("rank", this.rank.getSqlName());
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}