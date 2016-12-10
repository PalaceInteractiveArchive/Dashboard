package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 10/10/16
 */
public class PacketPlayerList extends BasePacket {
    private List<UUID> players = new ArrayList<>();

    public PacketPlayerList() {
        this(new ArrayList<UUID>());
    }

    public PacketPlayerList(List<UUID> players) {
        this.id = PacketID.Dashboard.PLAYERLIST.getID();
        this.players = players;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public PacketPlayerList fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        JsonArray list = obj.get("players").getAsJsonArray();

        for (JsonElement e : list) {
            this.players.add(UUID.fromString(e.getAsString()));
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            Gson gson = new Gson();

            obj.addProperty("id", this.id);
            obj.add("players", gson.toJsonTree(this.players).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}