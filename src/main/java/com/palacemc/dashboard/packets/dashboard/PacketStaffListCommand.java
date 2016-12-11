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
 * Created by Marc on 8/20/16
 */
public class PacketStaffListCommand extends BasePacket {
    @Getter private UUID uuid;
    @Getter private List<String> empress = new ArrayList<>();
    @Getter private List<String> emperors = new ArrayList<>();
    @Getter private List<String> wizards = new ArrayList<>();
    @Getter private List<String> paladins = new ArrayList<>();
    @Getter private List<String> knights = new ArrayList<>();
    @Getter private List<String> squires = new ArrayList<>();

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

    public PacketStaffListCommand fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        object.get("empress").getAsJsonArray().forEach(element -> this.empress.add(element.getAsString()));
        object.get("emperors").getAsJsonArray().forEach(element -> this.emperors.add(element.getAsString()));
        object.get("wizards").getAsJsonArray().forEach(element -> this.wizards.add(element.getAsString()));
        object.get("paladins").getAsJsonArray().forEach(element -> this.paladins.add(element.getAsString()));
        object.get("knights").getAsJsonArray().forEach(element -> this.knights.add(element.getAsString()));
        object.get("squires").getAsJsonArray().forEach(element -> this.squires.add(element.getAsString()));

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            Gson gson = new Gson();

            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.add("empress", gson.toJsonTree(this.empress).getAsJsonArray());
            object.add("emperors", gson.toJsonTree(this.emperors).getAsJsonArray());
            object.add("wizards", gson.toJsonTree(this.wizards).getAsJsonArray());
            object.add("paladins", gson.toJsonTree(this.paladins).getAsJsonArray());
            object.add("knights", gson.toJsonTree(this.knights).getAsJsonArray());
            object.add("squires", gson.toJsonTree(this.squires).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}