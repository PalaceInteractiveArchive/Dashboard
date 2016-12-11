package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

/**
 * Created by Marc on 6/15/15
 */
public class PacketGetPlayer extends BasePacket {
    private String playerName = "";

    public PacketGetPlayer() {
        this("");
    }

    public PacketGetPlayer(String playerName) {
        this.id = PacketID.GETPLAYER.getID();
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public PacketGetPlayer fromJSON(JsonObject object) {
        this.playerName = object.get("playerName").getAsString();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("playerName", this.playerName);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}