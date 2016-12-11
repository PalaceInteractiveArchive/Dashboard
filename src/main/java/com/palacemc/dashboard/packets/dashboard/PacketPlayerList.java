package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 10/10/16
 */
public class PacketPlayerList extends BasePacket {
    @Getter private List<UUID> players = new ArrayList<>();

    public PacketPlayerList() {
        this(new ArrayList<>());
    }

    public PacketPlayerList(List<UUID> players) {
        this.id = PacketID.Dashboard.PLAYERLIST.getID();
        this.players = players;
    }

    public PacketPlayerList fromJSON(JsonObject object) {
        this.id = object.get("id").getAsInt();
        object.get("players").getAsJsonArray().forEach(element ->
                this.players.add(UUID.fromString(element.getAsString())));

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