package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;
import lombok.Getter;

/**
 * Created by Marc on 7/14/16
 */
public class PacketConnectionType extends BasePacket {
    @Getter private ConnectionType type;

    public PacketConnectionType() {
        this(null);
    }

    public PacketConnectionType(ConnectionType type) {
        this.id = PacketID.Dashboard.CONNECTIONTYPE.getID();
        this.type = type;
    }

    public PacketConnectionType fromJSON(JsonObject object) {
        this.type = ConnectionType.fromString(object.get("type").getAsString());
        return this;
    }

    public JsonObject getJSON() {
        JsonObject object = new JsonObject();

        try {
            object.addProperty("id", this.id);
            object.addProperty("type", this.type.name().toLowerCase());
        } catch (Exception e) {
            return null;
        }

        return object;
    }

    public ConnectionType getType() {
        return type;
    }

    public enum ConnectionType {
        BUNGEECORD, DAEMON, WEBCLIENT, INSTANCE, AUDIOSERVER, UNKNOWN;

        public static ConnectionType fromString(String s) {
            switch (s.toLowerCase()) {
                case "bungeecord":
                    return BUNGEECORD;
                case "daemon":
                    return DAEMON;
                case "webclient":
                    return WEBCLIENT;
                case "instance":
                    return INSTANCE;
                case "audioserver":
                    return AUDIOSERVER;
            }

            return UNKNOWN;
        }
    }
}