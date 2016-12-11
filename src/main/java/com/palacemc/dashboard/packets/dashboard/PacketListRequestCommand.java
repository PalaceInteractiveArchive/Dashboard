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
 * Created by Marc on 8/22/16
 */
public class PacketListRequestCommand extends BasePacket {
    @Getter private UUID uuid;
    @Getter private List<String> requestlist = new ArrayList<>();

    public PacketListRequestCommand() {
        this(null, new ArrayList<String>());
    }

    public PacketListRequestCommand(UUID uuid, List<String> requestlist) {
        this.id = PacketID.Dashboard.LISTREQUESTCOMMAND.getID();
        this.uuid = uuid;
        this.requestlist = requestlist;
    }

    public PacketListRequestCommand fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        object.get("requestlist").getAsJsonArray().forEach(element -> this.requestlist.add(element.getAsString()));
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            Gson gson = new Gson();

            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.add("requestlist", gson.toJsonTree(this.requestlist).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}