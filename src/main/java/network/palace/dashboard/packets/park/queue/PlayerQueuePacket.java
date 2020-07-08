package network.palace.dashboard.packets.park.queue;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

@Getter
public class PlayerQueuePacket extends BasePacket {
    private String queueId;
    private UUID uuid;
    private boolean join;

    public PlayerQueuePacket() {
        this("", null);
    }

    public PlayerQueuePacket(String queueId, UUID uuid) {
        this.id = PacketID.Park.PLAYER_QUEUE.getID();
        this.queueId = queueId;
        this.uuid = uuid;
        this.join = join;
    }

    public PlayerQueuePacket fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.queueId = obj.get("queueId").getAsString();
        this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        this.join = obj.get("join").getAsBoolean();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("queueId", this.queueId);
        obj.addProperty("uuid", this.uuid.toString());
        obj.addProperty("join", this.join);
        return obj;
    }
}
