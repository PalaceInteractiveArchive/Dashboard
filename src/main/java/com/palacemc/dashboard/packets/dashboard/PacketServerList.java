package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marc on 8/25/16
 */
public class PacketServerList extends BasePacket {
    @Getter private List<String> servers = new ArrayList<>();

    public PacketServerList() {
        this(new ArrayList<>());
    }

    public PacketServerList(List<String> servers) {
        this.id = PacketID.Dashboard.SERVERLIST.getID();
        this.servers = servers;
    }

    public PacketServerList fromJSON(JsonObject obj) {
        obj.get("servers").getAsJsonArray().forEach(element -> this.servers.add(element.getAsString()));

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            Gson gson = new Gson();

            object.addProperty("id", this.id);
            object.add("servers", gson.toJsonTree(this.servers).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}