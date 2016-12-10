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
 * Created by Marc on 8/20/16
 */
public class PacketStaffListCommand extends BasePacket {
    private UUID uuid;
    private List<String> empress = new ArrayList<>();
    private List<String> emperors = new ArrayList<>();
    private List<String> wizards = new ArrayList<>();
    private List<String> paladins = new ArrayList<>();
    private List<String> knights = new ArrayList<>();
    private List<String> squires = new ArrayList<>();

    public PacketStaffListCommand() {
        this.id = PacketID.Dashboard.STAFFLISTCOMMAND.getID();
        uuid = null;
    }

    public PacketStaffListCommand(UUID uuid, List<String> empress, List<String> emperors, List<String> wizards,
                                  List<String> paladins, List<String> knights, List<String> squires) {
        this.id = PacketID.Dashboard.STAFFLISTCOMMAND.getID();
        this.uuid = uuid;
        this.empress = empress;
        this.emperors = emperors;
        this.wizards = wizards;
        this.paladins = paladins;
        this.knights = knights;
        this.squires = squires;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public List<String> getEmperors() {
        return emperors;
    }

    public List<String> getEmpress() {
        return empress;
    }

    public List<String> getWizards() {
        return wizards;
    }

    public List<String> getPaladins() {
        return paladins;
    }

    public List<String> getKnights() {
        return knights;
    }

    public List<String> getSquires() {
        return squires;
    }

    public PacketStaffListCommand fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        JsonArray may = obj.get("empress").getAsJsonArray();
        for (JsonElement e : may) {
            this.empress.add(e.getAsString());
        }

        JsonArray man = obj.get("emperors").getAsJsonArray();
        for (JsonElement e : man) {
            this.emperors.add(e.getAsString());
        }

        JsonArray dev = obj.get("wizards").getAsJsonArray();
        for (JsonElement e : dev) {
            this.wizards.add(e.getAsString());
        }

        JsonArray crd = obj.get("paladins").getAsJsonArray();
        for (JsonElement e : crd) {
            this.paladins.add(e.getAsString());
        }

        JsonArray cas = obj.get("knights").getAsJsonArray();
        for (JsonElement e : cas) {
            this.knights.add(e.getAsString());
        }

        JsonArray ear = obj.get("squires").getAsJsonArray();
        for (JsonElement e : ear) {
            this.squires.add(e.getAsString());
        }

        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            Gson gson = new Gson();

            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.add("empress", gson.toJsonTree(this.empress).getAsJsonArray());
            obj.add("emperors", gson.toJsonTree(this.emperors).getAsJsonArray());
            obj.add("wizards", gson.toJsonTree(this.wizards).getAsJsonArray());
            obj.add("paladins", gson.toJsonTree(this.paladins).getAsJsonArray());
            obj.add("knights", gson.toJsonTree(this.knights).getAsJsonArray());
            obj.add("squires", gson.toJsonTree(this.squires).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}