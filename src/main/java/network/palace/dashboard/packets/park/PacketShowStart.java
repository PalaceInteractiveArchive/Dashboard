package network.palace.dashboard.packets.park;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

@Getter
public class PacketShowStart extends BasePacket {
    private String showName;

    public PacketShowStart() {
        this("");
    }

    public PacketShowStart(String showName) {
        this.id = PacketID.Park.SHOW_START.getID();
        this.showName = showName;
    }

    public PacketShowStart fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.showName = obj.get("showName").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", this.id);
        obj.addProperty("showName", this.showName);
        return obj;
    }
}