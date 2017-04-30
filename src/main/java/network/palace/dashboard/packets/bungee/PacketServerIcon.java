package network.palace.dashboard.packets.bungee;

import com.google.gson.JsonObject;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

/**
 * Created by Marc on 4/30/17.
 */
public class PacketServerIcon extends BasePacket {
    private String base64;

    public PacketServerIcon() {
        this("");
    }

    public PacketServerIcon(String base64) {
        this.id = PacketID.Bungee.SERVERICON.getID();
        this.base64 = base64;
    }

    public String getBase64() {
        return base64;
    }

    public PacketServerIcon fromJSON(JsonObject obj) {
        this.base64 = obj.get("base64").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("base64", this.base64);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}