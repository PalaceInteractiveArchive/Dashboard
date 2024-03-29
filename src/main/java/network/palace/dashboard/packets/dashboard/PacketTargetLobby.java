package network.palace.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

/**
 * Created by Marc on 8/25/16
 */
public class PacketTargetLobby extends BasePacket {
    private String server;

    public PacketTargetLobby() {
        this("");
    }

    public PacketTargetLobby(String server) {
        this.id = PacketID.Dashboard.TARGETLOBBY.getID();
        this.server = server;
    }

    public String getServer() {
        return server;
    }

    public PacketTargetLobby fromJSON(JsonObject obj) {
        this.server = obj.get("server").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("server", this.server);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}