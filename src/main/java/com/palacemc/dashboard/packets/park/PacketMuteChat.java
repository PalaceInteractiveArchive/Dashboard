package com.palacemc.dashboard.packets.park;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 9/18/16
 */
public class PacketMuteChat extends BasePacket {
    @Getter private String server;
    @Getter private boolean mute;
    @Getter private String source;

    public PacketMuteChat() {
        this("", false, "");
    }

    public PacketMuteChat(String server, boolean mute, String source) {
        this.id = PacketID.Park.MUTECHAT.getId();
        this.server = server;
        this.mute = mute;
        this.source = source;
    }

    public PacketMuteChat fromJSON(JsonObject obj) {
        this.id = obj.get("id").getAsInt();
        this.server = obj.get("server").getAsString();
        this.mute = obj.get("mute").getAsBoolean();
        this.source = obj.get("source").getAsString();

        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        obj.addProperty("id", this.id);
        obj.addProperty("server", this.server);
        obj.addProperty("mute", this.mute);
        obj.addProperty("source", this.source);

        return obj;
    }
}