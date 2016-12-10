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
 * Created by Marc on 8/22/16
 */
public class PacketListRequestCommand extends BasePacket {
    private UUID uuid;
    private List<String> requestlist = new ArrayList<>();

    public PacketListRequestCommand() {
        this(null, new ArrayList<String>());
    }

    public PacketListRequestCommand(UUID uuid, List<String> requestlist) {
        this.id = PacketID.Dashboard.LISTREQUESTCOMMAND.getID();
        this.uuid = uuid;
        this.requestlist = requestlist;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public List<String> getRequestlist() {
        return requestlist;
    }

    public PacketListRequestCommand fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        JsonArray list = obj.get("requestlist").getAsJsonArray();
        for (JsonElement e : list) {
            this.requestlist.add(e.getAsString());
        }
        return this;
    }

    public JsonObject getJSON() {

        JsonObject obj = new JsonObject();
        try {
            Gson gson = new Gson();

            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.add("requestlist", gson.toJsonTree(this.requestlist).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }

        return obj;
    }
}