package network.palace.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.SponsorTier;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 9/17/16
 */
public class PacketRankChange extends BasePacket {
    private UUID uuid;
    @Getter private Rank rank;
    @Getter private SponsorTier tier;
    @Getter private String source;

    public PacketRankChange() {
        this(null, Rank.SETTLER, SponsorTier.NONE, "");
    }

    public PacketRankChange(UUID uuid, Rank rank, SponsorTier tier, String source) {
        this.id = PacketID.Dashboard.RANKCHANGE.getID();
        this.uuid = uuid;
        this.rank = rank;
        this.tier = tier;
        this.source = source;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PacketRankChange fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.rank = Rank.fromString(obj.get("rank").getAsString());
        this.tier = SponsorTier.fromString(obj.get("tier").getAsString());
        this.source = obj.get("source").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("rank", this.rank.getDBName());
            obj.addProperty("tier", this.tier.getDBName());
            obj.addProperty("source", this.source);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}