package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import com.palacemc.dashboard.handlers.Rank;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by Marc on 9/17/16
 */
public class PacketRankChange extends BasePacket {
    @Getter private UUID uuid;
    @Getter private Rank rank;
    @Getter private String source;

    public PacketRankChange() {
        this(null, Rank.SETTLER, "");
    }

    public PacketRankChange(UUID uuid, Rank rank, String source) {
        this.id = PacketID.Dashboard.RANKCHANGE.getID();
        this.uuid = uuid;
        this.rank = rank;
        this.source = source;
    }

    public PacketRankChange fromJSON(JsonObject object) {
        this.id = object.get("id").getAsInt();

        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.rank = Rank.fromString(object.get("rank").getAsString());
        this.source = object.get("source").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("rank", this.rank.getSqlName());
            object.addProperty("source", this.source);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}