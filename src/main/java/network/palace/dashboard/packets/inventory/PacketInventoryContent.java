package network.palace.dashboard.packets.inventory;

import com.google.gson.JsonObject;
import lombok.*;
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
    @Getter private ResortType resortType;
    @Getter private String inventoryJson;
    @Getter private String inventoryHash;

    @Getter private String lockerJson;
    @Getter private String lockerHash;

    @Getter private String hotbarJson;
    @Getter private String hotbarHash;

    @Override
    public PacketInventoryContent fromJSON(JsonObject obj) {
        this.id = PacketID.Inventory.INVENTORY_CONTENT.getID();
        this.uuid = UUID.fromString(obj.get("uuid").getAsString());
        this.resortType = ResortType.fromId(obj.get("resortType").getAsInt());
        this.inventoryJson = obj.get("inventoryJson").getAsString();
        this.inventoryHash = obj.get("inventoryHash").getAsString();
        return this;
    }

    @Override
    public JsonObject getJSON() {
        JsonObject obj = new JsonObject();
        try {
            obj.addProperty("id", this.id);
            obj.addProperty("uuid", this.uuid.toString());
            obj.addProperty("resortType", this.resortType.getId());
            obj.addProperty("inventoryJson", this.inventoryJson);
            obj.addProperty("inventoryHash", this.inventoryHash);
        } catch (Exception e) {
            return null;
        }
        return obj;
    }
}
