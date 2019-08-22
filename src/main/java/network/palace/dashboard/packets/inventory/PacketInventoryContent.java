package network.palace.dashboard.packets.inventory;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * @author Innectic
 * @since 6/10/2017
 */
public class PacketInventoryContent extends BasePacket {
    @Getter private UUID uuid;
    @Getter private Resort resort;
    @Getter @Setter private boolean disconnect = false;

    @Getter private String backpackJson;
    @Getter private String backpackHash;
    @Getter private int backpackSize;

    @Getter private String lockerJson;
    @Getter private String lockerHash;
    @Getter private int lockerSize;

    @Getter private String baseJson;
    @Getter private String baseHash;

    @Getter private String buildJson;
    @Getter private String buildHash;

    public PacketInventoryContent() {
        this(null, Resort.WDW, "", "", -1, "", "", -1, "", "", "", "");
    }

    public PacketInventoryContent(UUID uuid, Resort resort, String backpackJson, String backpackHash,
                                  int backpackSize, String lockerJson, String lockerHash, int lockerSize,
                                  String baseJson, String baseHash, String buildJson, String buildHash) {
        this.id = PacketID.Inventory.INVENTORY_CONTENT.getID();
        this.uuid = uuid;
        this.resort = resort;
        this.backpackJson = backpackJson;
        this.backpackHash = backpackHash;
        this.backpackSize = backpackSize;
        this.lockerJson = lockerJson;
        this.lockerHash = lockerHash;
        this.lockerSize = lockerSize;
        this.baseJson = baseJson;
        this.baseHash = baseHash;
        this.buildJson = buildJson;
        this.buildHash = buildHash;
    }

    public boolean isEmpty() {
        return backpackHash.isEmpty() && lockerHash.isEmpty() && baseHash.isEmpty() && buildHash.isEmpty() && backpackSize < 0 && lockerSize < 0;
    }

    @Override
    public PacketInventoryContent fromJSON(JsonObject obj) {
        this.id = PacketID.Inventory.INVENTORY_CONTENT.getID();
        this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        this.resort = Resort.fromId(obj.get("resort").getAsInt());
        this.disconnect = obj.get("disconnect").getAsBoolean();

        if (obj.get("backpackJson").isJsonNull()) {
            this.backpackJson = "";
        } else {
            this.backpackJson = obj.get("backpackJson").getAsString();
        }
        if (obj.get("backpackHash").isJsonNull()) {
            this.backpackHash = "";
        } else {
            this.backpackHash = obj.get("backpackHash").getAsString();
        }
        if (obj.get("backpackSize").isJsonNull()) {
            this.backpackSize = -1;
        } else {
            this.backpackSize = obj.get("backpackSize").getAsInt();
        }

        if (obj.get("lockerJson").isJsonNull()) {
            this.lockerJson = "";
        } else {
            this.lockerJson = obj.get("lockerJson").getAsString();
        }
        if (obj.get("lockerHash").isJsonNull()) {
            this.lockerHash = "";
        } else {
            this.lockerHash = obj.get("lockerHash").getAsString();
        }
        if (obj.get("lockerSize").isJsonNull()) {
            this.lockerSize = -1;
        } else {
            this.lockerSize = obj.get("lockerSize").getAsInt();
        }

        if (obj.get("baseJson").isJsonNull()) {
            this.baseJson = "";
        } else {
            this.baseJson = obj.get("baseJson").getAsString();
        }
        if (obj.get("baseHash").isJsonNull()) {
            this.baseHash = "";
        } else {
            this.baseHash = obj.get("baseHash").getAsString();
        }

        if (obj.get("buildJson").isJsonNull()) {
            this.buildJson = "";
        } else {
            this.buildJson = obj.get("buildJson").getAsString();
        }
        if (obj.get("buildHash").isJsonNull()) {
            this.buildHash = "";
        } else {
            this.buildHash = obj.get("buildHash").getAsString();
        }
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("resort", this.resort.getId());
            obj.addProperty("disconnect", this.disconnect);
            obj.addProperty("backpackJson", this.backpackJson);
            obj.addProperty("backpackHash", this.backpackHash);
            obj.addProperty("backpackSize", this.backpackSize);
            obj.addProperty("lockerJson", this.lockerJson);
            obj.addProperty("lockerHash", this.lockerHash);
            obj.addProperty("lockerSize", this.lockerSize);
            obj.addProperty("baseJson", this.baseJson);
            obj.addProperty("baseHash", this.baseHash);
            obj.addProperty("buildJson", this.buildJson);
            obj.addProperty("buildHash", this.buildHash);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
