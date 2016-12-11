package com.palacemc.dashboard.packets.audio;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 6/15/15
 */
public class PacketExecScript extends BasePacket {
    @Getter private String script = "";

    public PacketExecScript() {
        this("");
    }

    public PacketExecScript(String script) {
        this.id = PacketID.EXEC_SCRIPT.getID();

        this.script = script;
    }

    public PacketExecScript fromJSON(JsonObject object) {
        try {
            this.script = object.get("script").getAsString();
        } catch (Exception e) {
            return null;
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("script", this.script);
        } catch (Exception e) {
            return null;
        }
        return object;
    }
}