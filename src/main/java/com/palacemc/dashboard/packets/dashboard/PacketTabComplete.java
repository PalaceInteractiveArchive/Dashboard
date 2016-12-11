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
 * Created by Marc on 9/3/16
 */
public class PacketTabComplete extends BasePacket {
    @Getter private UUID uuid;
    @Getter private String command;
    @Getter private List<String> args;
    @Getter private List<String> results = new ArrayList<>();

    public PacketTabComplete() {
        this(null, "", new ArrayList<>(), new ArrayList<>());
    }

    public PacketTabComplete(UUID uuid, String command, List<String> args, List<String> results) {
        this.id = PacketID.Dashboard.TABCOMPLETE.getID();
        this.uuid = uuid;
        this.command = command;
        this.args = args;
        this.results = results;
    }

    public PacketTabComplete fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.command = object.get("command").getAsString();

        object.get("args").getAsJsonArray().forEach(element -> this.args.add(element.getAsString()));
        object.get("results").getAsJsonArray().forEach(element -> this.results.add(element.getAsString()));

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();
        try {
            Gson gson = new Gson();

            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("command", this.command);
            object.add("args", gson.toJsonTree(this.args).getAsJsonArray());
            object.add("results", gson.toJsonTree(this.results).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}