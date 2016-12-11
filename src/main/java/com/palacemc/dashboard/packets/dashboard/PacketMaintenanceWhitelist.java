package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 9/12/16
 */
public class PacketMaintenanceWhitelist extends BasePacket {
    @Getter private List<UUID> allowed = new ArrayList<>();

    public PacketMaintenanceWhitelist() {
        this(new ArrayList<UUID>());
    }

    public PacketMaintenanceWhitelist(List<UUID> allowed) {
        this.id = PacketID.Dashboard.MAINTENANCELIST.getID();
        this.allowed = allowed;
    }

    public PacketMaintenanceWhitelist fromJSON(JsonObject object) {
        JsonArray list = object.get("allowed").getAsJsonArray();

        for (JsonElement e : list) {
            try {
                this.allowed.add(UUID.fromString(e.getAsString()));
            } catch (Exception ignored) {
            }
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            Gson gson = new Gson();
            object.add("allowed", gson.toJsonTree(this.allowed).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}