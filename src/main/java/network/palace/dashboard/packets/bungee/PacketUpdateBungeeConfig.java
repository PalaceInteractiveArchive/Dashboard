package network.palace.dashboard.packets.bungee;

import com.google.gson.JsonObject;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

public class PacketUpdateBungeeConfig extends BasePacket {

    public PacketUpdateBungeeConfig() {
        this.id = PacketID.Bungee.UPDATEBUNGEECONFIG.getID();
    }

    public PacketUpdateBungeeConfig fromJSON(JsonObject obj) {
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}