package network.palace.dashboard.packets.dashboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.HashMap;
import java.util.Map;

@Getter
public class PacketLogStatistic extends BasePacket {
    private String tableName;
    private HashMap<String, String> values;

    public PacketLogStatistic() {
        this(null, new HashMap<>());
    }

    public PacketLogStatistic(String tableName, HashMap<String, String> values) {
        this.id = PacketID.Dashboard.LOG_STATISTIC.getID();
        this.tableName = tableName;
        this.values = values;
    }

    public PacketLogStatistic fromJSON(JsonObject obj) {
        this.tableName = obj.get("table-name").getAsString();
        JsonArray values = obj.getAsJsonArray("values");
        for (JsonElement e : values) {
            JsonObject o = (JsonObject) e;
            this.values.put(o.get("field-name").getAsString(), o.get("value").getAsString());
        }
        return this;
    }

    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("table-name", tableName);
            JsonArray values = new JsonArray();
            for (Map.Entry<String, String> entry : this.values.entrySet()) {
                JsonObject object = new JsonObject();
                object.addProperty("field-name", entry.getKey());
                object.addProperty("value", entry.getValue());
                values.add(object);
            }
            obj.add("values", values);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
