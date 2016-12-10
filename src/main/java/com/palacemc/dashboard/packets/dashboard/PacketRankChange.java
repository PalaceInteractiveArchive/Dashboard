package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import com.palacemc.dashboard.handlers.Rank;

import java.util.UUID;

/**
 * Created by Marc on 9/17/16
 */
public class PacketRankChange extends BasePacket {
    private UUID uuid;
    private Rank rank;
    private String source;

    public PacketRankChange() {
        this(null, Rank.SETTLER, "");
    }

    public PacketRankChange(UUID uuid, Rank rank, String source) {
        this.id = PacketID.Dashboard.RANKCHANGE.getID();
        this.uuid = uuid;
        this.rank = rank;
        this.source = source;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Rank getRank() {
        return rank;
    }

    public String getSource() {
        return source;
    }

    public PacketRankChange fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();

        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.rank = Rank.fromString(obj.get("rank").getAsString());
        this.source = obj.get("source").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("rank", this.rank.getSqlName());
            obj.addProperty("source", this.source);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}