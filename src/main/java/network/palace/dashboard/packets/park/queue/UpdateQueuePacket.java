package network.palace.dashboard.packets.park.queue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class UpdateQueuePacket extends BasePacket {
    private String queueId;
    private boolean open;
    private List<UUID> queue;

    public UpdateQueuePacket() {
        this("", false, new ArrayList<>());
    }

    public UpdateQueuePacket(String queueId, boolean open, List<UUID> queue) {
        this.id = PacketID.Park.UPDATE_QUEUE.getID();
        this.queueId = queueId;
        this.open = open;
        this.queue = queue;
    }

    public UpdateQueuePacket fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.queueId = obj.get("queueId").getAsString();
        this.open = obj.get("open").getAsBoolean();
        queue = new ArrayList<>();
        if (obj.has("queue")) {
            JsonArray queueJSON = obj.get("queue").getAsJsonArray();
            for (JsonElement e : queueJSON) {
                try {
                    queue.add(UUID.fromString(e.getAsString()));
                } catch (Exception ignored) {
                }
            }
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("queueId", this.queueId);
        obj.addProperty("open", this.open);
        if (queue != null) {
            try {
                Gson gson = new Gson();
                List<String> list = new ArrayList<>();
                for (UUID uuid : queue) {
                    list.add(uuid.toString());
                }
                obj.add("queue", gson.toJsonTree(list).getAsJsonArray());
            } catch (Exception e) {
                return null;
            }
        }
        return obj;
    }
}
