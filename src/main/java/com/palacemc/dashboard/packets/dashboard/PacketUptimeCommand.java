package com.palacemc.dashboard.packets.dashboard;

import com.google.gson.JsonObject;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * Created by Marc on 8/26/16
 */
public class PacketUptimeCommand extends BasePacket {
    private UUID uuid;
    private long time;

    public PacketUptimeCommand() {
        this(null, 0);
    }

    public PacketUptimeCommand(UUID uuid, long time) {
        this.id = PacketID.Dashboard.UPTIMECOMMAND.getID();
        this.uuid = uuid;
        this.time = time;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public long getTime() {
        return time;
    }

    public PacketUptimeCommand fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }

        this.time = obj.get("time").getAsLong();
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();

        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("time", this.time);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}