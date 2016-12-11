package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketServerSwitch extends BasePacket {
    @Getter private String servername = "";

    public PacketServerSwitch(String servername) {
        this.id = PacketID.SERVER_SWITCH.getID();

        this.servername = servername;
    }

    public PacketServerSwitch fromJSON(JsonObject object) {
        try {
            this.servername = object.get("servername").getAsString();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("servername", this.servername);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}