package network.palace.dashboard.packets.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 8/20/16
 */
@Getter
public class PacketStaffListCommand extends BasePacket {
    private UUID uuid;
    private List<String> directors = new ArrayList<>();
    private List<String> managers = new ArrayList<>();
    private List<String> admins = new ArrayList<>();
    private List<String> developers = new ArrayList<>();
    private List<String> coordinators = new ArrayList<>();
    private List<String> architects = new ArrayList<>();
    private List<String> builders = new ArrayList<>();
    private List<String> mods = new ArrayList<>();
    private List<String> trainees = new ArrayList<>();

    public PacketStaffListCommand() {
        this.id = PacketID.Dashboard.STAFFLISTCOMMAND.getID();
        uuid = null;
    }

    public PacketStaffListCommand(UUID uuid, List<String> directors, List<String> managers, List<String> admins,
                                  List<String> developers, List<String> coordinators, List<String> architects,
                                  List<String> builders, List<String> mods, List<String> trainees) {
        this.id = PacketID.Dashboard.STAFFLISTCOMMAND.getID();
        this.uuid = uuid;
        this.directors = directors;
        this.managers = managers;
        this.admins = admins;
        this.developers = developers;
        this.coordinators = coordinators;
        this.architects = architects;
        this.builders = builders;
        this.mods = mods;
        this.trainees = trainees;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PacketStaffListCommand fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        JsonArray dir = obj.get("directors").getAsJsonArray();
        for (JsonElement e : dir) {
            this.directors.add(e.getAsString());
        }
        JsonArray man = obj.get("managers").getAsJsonArray();
        for (JsonElement e : man) {
            this.managers.add(e.getAsString());
        }
        JsonArray adm = obj.get("admins").getAsJsonArray();
        for (JsonElement e : adm) {
            this.admins.add(e.getAsString());
        }
        JsonArray dev = obj.get("developers").getAsJsonArray();
        for (JsonElement e : dev) {
            this.developers.add(e.getAsString());
        }
        JsonArray coord = obj.get("coordinators").getAsJsonArray();
        for (JsonElement e : coord) {
            this.coordinators.add(e.getAsString());
        }
        JsonArray arch = obj.get("architects").getAsJsonArray();
        for (JsonElement e : arch) {
            this.architects.add(e.getAsString());
        }
        JsonArray bldr = obj.get("builders").getAsJsonArray();
        for (JsonElement e : bldr) {
            this.builders.add(e.getAsString());
        }
        JsonArray mod = obj.get("mods").getAsJsonArray();
        for (JsonElement e : mod) {
            this.mods.add(e.getAsString());
        }
        JsonArray trn = obj.get("trainees").getAsJsonArray();
        for (JsonElement e : trn) {
            this.trainees.add(e.getAsString());
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            Gson gson = new Gson();
            obj.add("directors", gson.toJsonTree(this.directors).getAsJsonArray());
            obj.add("managers", gson.toJsonTree(this.managers).getAsJsonArray());
            obj.add("admins", gson.toJsonTree(this.admins).getAsJsonArray());
            obj.add("developers", gson.toJsonTree(this.developers).getAsJsonArray());
            obj.add("coordinators", gson.toJsonTree(this.coordinators).getAsJsonArray());
            obj.add("architects", gson.toJsonTree(this.architects).getAsJsonArray());
            obj.add("builders", gson.toJsonTree(this.builders).getAsJsonArray());
            obj.add("mods", gson.toJsonTree(this.mods).getAsJsonArray());
            obj.add("trainees", gson.toJsonTree(this.trainees).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}