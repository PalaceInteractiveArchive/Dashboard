package com.palacemc.dashboard.packets.arcade;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 11/18/16
 */
public class PacketGameStatus extends BasePacket {
    @Getter private String serverName;
    @Getter private int count;
    @Getter private String state;

    public PacketGameStatus() {
        this("", 0, "");
    }

    public PacketGameStatus(String serverName, int count, String state) {
        this.id = PacketID.Arcade.GAMESTATUS.getId();
        this.serverName = serverName;
        this.count = count;
        this.state = state;
    }

    public PacketGameStatus fromJSON(JsonObject object) {
        this.id = object.get("id").getAsInt();
        this.serverName = object.get("servername").getAsString();
        this.count = object.get("count").getAsInt();
        this.state = object.get("state").getAsString();

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("servername", this.serverName);
            object.addProperty("count", this.count);
            object.addProperty("state", this.state);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}
