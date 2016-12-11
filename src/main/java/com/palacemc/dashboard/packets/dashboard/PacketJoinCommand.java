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
 * Created by Marc on 8/26/16
 */
public class PacketJoinCommand extends BasePacket {
    @Getter private UUID uuid;
    @Getter private List<String> servers = new ArrayList<>();

    public PacketJoinCommand() {
        this(null, new ArrayList<>());
    }

    public PacketJoinCommand(UUID uuid, List<String> servers) {
        this.id = PacketID.Dashboard.JOINCOMMAND.getID();
        this.uuid = uuid;
        this.servers = servers;
    }

    public PacketJoinCommand fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        obj.get("servers").getAsJsonArray().forEach(element -> this.servers.add(element.getAsString()));
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            Gson gson = new Gson();

            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.add("servers", gson.toJsonTree(this.servers).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}