package network.palace.dashboard.packets.dashboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 8/20/16
 */
public class PacketStaffListCommand extends BasePacket {
    private UUID uuid;
    private List<String> managers = new ArrayList<>();
    private List<String> admins = new ArrayList<>();
    private List<String> developers = new ArrayList<>();
    private List<String> srmods = new ArrayList<>();
    private List<String> mods = new ArrayList<>();
    private List<String> trainees = new ArrayList<>();

    public PacketStaffListCommand() {
        this.id = PacketID.Dashboard.STAFFLISTCOMMAND.getID();
        uuid = null;
    }

    public PacketStaffListCommand(UUID uuid, List<String> managers, List<String> admins, List<String> developers,
                                  List<String> srmods, List<String> mods, List<String> trainees) {
        this.id = PacketID.Dashboard.STAFFLISTCOMMAND.getID();
        this.uuid = uuid;
        this.managers = managers;
        this.admins = admins;
        this.developers = developers;
        this.srmods = srmods;
        this.mods = mods;
        this.trainees = trainees;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public List<String> getManagers() {
        return managers;
    }

    public List<String> getDevelopers() {
        return developers;
    }

    public List<String> getSrmods() {
        return srmods;
    }

    public List<String> getMods() {
        return mods;
    }

    public List<String> getTrainees() {
        return trainees;
    }

    public PacketStaffListCommand fromJSON(JsonObject obj) {
        try {
            this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        } catch (Exception e) {
            this.uuid = null;
        }
        JsonArray may = obj.get("managers").getAsJsonArray();
        for (JsonElement e : may) {
            this.managers.add(e.getAsString());
        }
        JsonArray man = obj.get("admins").getAsJsonArray();
        for (JsonElement e : man) {
            this.admins.add(e.getAsString());
        }
        JsonArray dev = obj.get("developers").getAsJsonArray();
        for (JsonElement e : dev) {
            this.developers.add(e.getAsString());
        }
        JsonArray crd = obj.get("srmods").getAsJsonArray();
        for (JsonElement e : crd) {
            this.srmods.add(e.getAsString());
        }
        JsonArray cas = obj.get("mods").getAsJsonArray();
        for (JsonElement e : cas) {
            this.mods.add(e.getAsString());
        }
        JsonArray ear = obj.get("trainees").getAsJsonArray();
        for (JsonElement e : ear) {
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
            obj.add("managers", gson.toJsonTree(this.managers).getAsJsonArray());
            obj.add("admins", gson.toJsonTree(this.admins).getAsJsonArray());
            obj.add("developers", gson.toJsonTree(this.developers).getAsJsonArray());
            obj.add("srmods", gson.toJsonTree(this.srmods).getAsJsonArray());
            obj.add("mods", gson.toJsonTree(this.mods).getAsJsonArray());
            obj.add("trainees", gson.toJsonTree(this.trainees).getAsJsonArray());
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}