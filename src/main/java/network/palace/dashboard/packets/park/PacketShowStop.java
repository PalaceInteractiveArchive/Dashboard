package network.palace.dashboard.packets.park;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

@Getter
public class PacketShowStop extends BasePacket {
    private String showName, world;

    public PacketShowStop() {
        this("", "");
    }

    public PacketShowStop(String showName, String world) {
        this.id = PacketID.Park.SHOW_STOP.getID();
        this.showName = showName;
        this.world = world;
    }

    public PacketShowStop fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.showName = obj.get("showName").getAsString();
        this.world = obj.get("world").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("showName", this.showName);
        obj.addProperty("world", this.world);
        return obj;
    }
}