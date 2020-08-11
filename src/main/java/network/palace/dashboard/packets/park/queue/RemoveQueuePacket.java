package network.palace.dashboard.packets.park.queue;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

@Getter
public class RemoveQueuePacket extends BasePacket {
    private String queueId;

    public RemoveQueuePacket() {
        this("");
    }

    public RemoveQueuePacket(String queueId) {
        this.id = PacketID.Park.REMOVE_QUEUE.getID();
        this.queueId = queueId;
    }

    public RemoveQueuePacket fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.queueId = obj.get("queueId").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("queueId", this.queueId);
        return obj;
    }
}
