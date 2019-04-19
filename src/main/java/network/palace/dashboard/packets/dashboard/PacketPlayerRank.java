package network.palace.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.SponsorTier;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

public class PacketPlayerRank extends BasePacket {
    private UUID uuid;
    @Getter private Rank rank;
    @Getter private SponsorTier tier;

    public PacketPlayerRank() {
        this(null, Rank.SETTLER, SponsorTier.NONE);
    }

    public PacketPlayerRank(UUID uuid, Rank rank, SponsorTier tier) {
        this.id = PacketID.Dashboard.PLAYERRANK.getID();
        this.uuid = uuid;
        this.rank = rank;
        this.tier = tier;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PacketPlayerRank fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.rank = Rank.fromString(obj.get("rank").getAsString());
        this.tier = SponsorTier.fromString(obj.get("tier").getAsString());
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("rank", this.rank.getDBName());
            obj.addProperty("tier", this.tier.getDBName());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}