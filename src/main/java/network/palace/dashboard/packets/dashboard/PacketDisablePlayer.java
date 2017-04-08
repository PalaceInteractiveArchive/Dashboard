package network.palace.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 4/8/17.
 */
public class PacketDisablePlayer extends BasePacket {
    private UUID uuid;
    @Getter private boolean disabled;

    public PacketDisablePlayer() {
        this(null, false);
    }

    public PacketDisablePlayer(UUID uuid, boolean disabled) {
        this.id = PacketID.Dashboard.DISABLEPLAYER.getID();
        this.uuid = uuid;
        this.disabled = disabled;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PacketDisablePlayer fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.disabled = obj.get("disabled").getAsBoolean();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("disabled", this.disabled);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}