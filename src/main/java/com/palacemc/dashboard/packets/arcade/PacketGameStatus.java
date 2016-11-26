package com.palacemc.dashboard.packets.arcade;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 11/18/16
 */
public class PacketGameStatus extends BasePacket {
    private String serverName;
    private int count;
    private String state;

    public PacketGameStatus() {
        this("", 0, "");
    }

    public PacketGameStatus(String serverName, int count, String state) {
        this.id = PacketID.Arcade.GAMESTATUS.getID();
        this.serverName = serverName;
        this.count = count;
        this.state = state;
    }

    public String getServerName() {
        return serverName;
    }

    public int getCount() {
        return count;
    }

    public String getState() {
        return state;
    }

    public PacketGameStatus fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.serverName = obj.get("servername").getAsString();
        this.count = obj.get("count").getAsInt();
        this.state = obj.get("state").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("servername", this.serverName);
            obj.addProperty("count", this.count);
            obj.addProperty("state", this.state);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
