package network.palace.dashboard.utils;

import network.palace.dashboard.packets.inventory.PacketInventoryContent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Innectic
 * @since 6/10/2017
 */
public class InventoryUtil {
    private Map<UUID, PacketInventoryContent> cachedInventories = new HashMap<>();

    /**
     * Cache a player's inventory
     *
     * @param uuid the uuid of the player to cache
     * @param inventory the player's inventory
     */
    public void cacheInventory(UUID uuid, PacketInventoryContent inventory) {
        cachedInventories.put(uuid, inventory);
    }

    /**
     * Get the inventory for a player
     *
     * @param uuid the uuid of the player
     * @return the inventory of the player. Defaults to a blank string if none is present
     */
    public PacketInventoryContent getInventory(UUID uuid) {
        return cachedInventories.getOrDefault(uuid, new PacketInventoryContent());
    }

    /**
     * Remove an inventory hash
     *
     * @param uuid the uuid who's hash to remove
     */
    public void removeHash(UUID uuid) {
        if (cachedInventories.containsKey(uuid)) {
            cachedInventories.remove(uuid);
        }
    }
}
