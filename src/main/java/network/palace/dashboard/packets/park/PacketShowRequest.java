package network.palace.dashboard.packets.park;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

@Getter
public class PacketShowRequest extends BasePacket {
    private UUID requestId;
    private UUID uuid;
    private String showName;
    private String server;

    public PacketShowRequest() {
        this(null, null, "", "");
    }

    public PacketShowRequest(UUID requestId, UUID uuid, String showName, String server) {
        this.id = PacketID.Park.SHOW_REQUEST.getID();
        this.requestId = requestId;
        this.uuid = uuid;
        this.showName = showName;
        this.server = server;
    }

    public PacketShowRequest fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        try {
            this.requestId = UUID.fromString(obj.get("requestId").getAsString());
        } catch (Exception e) {
            this.requestId = null;
        }
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        this.showName = obj.get("showName").getAsString();
        this.server = obj.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("requestId", this.requestId.toString());
        obj.addProperty("uuid", this.uuid.toString());
        obj.addProperty("showName", this.showName);
        obj.addProperty("server", this.server);
        return obj;
    }
}