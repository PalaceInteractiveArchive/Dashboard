package network.palace.dashboard.packets.inventory;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.PacketID;

import java.util.UUID;

/**
 * @author Innectic
 * @since 6/10/2017
 */
@AllArgsConstructor
@NoArgsConstructor
public class PacketInventoryContent extends BasePacket {
    @Getter private UUID uuid;
    @Getter private Resort resort;
    @Getter private String backpackJson;
    @Getter private String backpackHash;

    @Getter private String lockerJson;
    @Getter private String lockerHash;

    @Getter private String hotbarJson;
    @Getter private String hotbarHash;

    @Override
    public PacketInventoryContent fromJSON(JsonObject obj) {
        this.id = PacketID.Inventory.INVENTORY_CONTENT.getID();
        this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        this.resort = Resort.fromId(obj.get("resortType").getAsInt());
        this.backpackJson = obj.get("backpackJson").getAsString();
        this.backpackHash = obj.get("backpackHash").getAsString();
        this.lockerJson = obj.get("lockerJson").getAsString();
        this.lockerHash = obj.get("lockerHash").getAsString();
        this.hotbarJson = obj.get("hotbarJson").getAsString();
        this.hotbarHash = obj.get("hotbarHash").getAsString();
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("resortType", this.resort.getId());
            obj.addProperty("backpackJson", this.backpackJson);
            obj.addProperty("backpackHash", this.backpackHash);
            obj.addProperty("lockerJson", this.lockerJson);
            obj.addProperty("lockerHash", this.lockerHash);
            obj.addProperty("hotbarJson", this.hotbarJson);
            obj.addProperty("hotbarHash", this.hotbarHash);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
