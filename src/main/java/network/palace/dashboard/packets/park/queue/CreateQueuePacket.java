package network.palace.dashboard.packets.park.queue;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

@Getter
public class CreateQueuePacket extends BasePacket {
    private String queueId, queueName;
    private int holdingArea;

    public CreateQueuePacket() {
        this("", "", 1);
    }

    public CreateQueuePacket(String queueId, String queueName, int holdingArea) {
        this.id = PacketID.Park.CREATE_QUEUE.getID();
        this.queueId = queueId;
        this.queueName = queueName;
        this.holdingArea = holdingArea;
    }

    public CreateQueuePacket fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.queueId = obj.get("queueId").getAsString();
        this.queueName = obj.get("queueName").getAsString();
        this.holdingArea = obj.get("holdingArea").getAsInt();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("queueId", this.queueId);
        obj.addProperty("queueName", this.queueName);
        obj.addProperty("holdingArea", this.holdingArea);
        return obj;
    }
}
