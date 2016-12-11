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
public class PacketListFriendCommand extends BasePacket {
    @Getter private UUID uuid;
    @Getter private int page;
    @Getter private int maxpage;
    @Getter private List<String> friendlist = new ArrayList<>();

    public PacketListFriendCommand() {
        this(null, 0, 0, new ArrayList<String>());
    }

    public PacketListFriendCommand(UUID uuid, int page, int maxpage, List<String> friendlist) {
        this.id = PacketID.Dashboard.LISTFRIENDCOMMAND.getID();
        this.uuid = uuid;
        this.page = page;
        this.maxpage = maxpage;
        this.friendlist = friendlist;
    }

    public PacketListFriendCommand fromJSON(JsonObject object) {
        try {
            this.uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.page = object.get("page").getAsInt();
        this.maxpage = object.get("maxpage").getAsInt();

        object.get("friendlist").getAsJsonArray().forEach(element -> this.friendlist.add(element.getAsString()));
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            Gson gson = new Gson();

            object.addProperty("id", this.id);
            object.addProperty("uuid", this.uuid.toString());
            object.addProperty("page", this.page);
            object.addProperty("maxpage", this.maxpage);
            object.add("friendlist", gson.toJsonTree(this.friendlist).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }

        return object;
    }
}