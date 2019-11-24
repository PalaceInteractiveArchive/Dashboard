package network.palace.dashboard.packets.park;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

@Getter
public class PacketShowRequestResponse extends BasePacket {
    private UUID requestId;

    public PacketShowRequestResponse() {
        this(null);
    }

    public PacketShowRequestResponse(UUID requestId) {
        this.id = PacketID.Park.SHOW_REQUEST_RESPONSE.getID();
        this.requestId = requestId;
    }

    public PacketShowRequestResponse fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        try {
            this.requestId = UUID.fromString(obj.get("requestId").getAsString());
        } catch (Exception e) {
            this.requestId = null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("requestId", this.requestId.toString());
        return obj;
    }
}