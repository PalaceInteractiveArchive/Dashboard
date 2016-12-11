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
 * Created by Marc on 9/10/16
 */
public class PacketIPSeenCommand extends BasePacket {
    @Getter private UUID uuid;
    @Getter private List<String> usernames = new ArrayList<>();
    @Getter private String address;

    public PacketIPSeenCommand() {
        this(null, new ArrayList<>(), "");
    }

    public PacketIPSeenCommand(UUID uuid, List<String> usernames, String address) {
        this.id = PacketID.Dashboard.IPSEENCOMMAND.getID();
        this.uuid = uuid;
        this.usernames = usernames;
        this.address = address;
    }

    public PacketIPSeenCommand fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        object.get("usernames").getAsJsonArray().forEach(element -> this.usernames.add(element.getAsString()));
        this.address = object.get("address").getAsString();

        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            Gson gson = new Gson();

            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.add("usernames", gson.toJsonTree(this.usernames).getAsJsonArray());
            object.addProperty("address", this.address);
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}