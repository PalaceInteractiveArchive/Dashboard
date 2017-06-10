package network.palace.dashboard.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Innectic
 * @since 6/10/2017
 */
public class InventoryUtil {
    private Map<UUID, String> cachedInventories = new HashMap<>();
    private Map<UUID, String> inventoryHash = new HashMap<>();

    /**
     * Cache a player's inventory
     *
     * @param uuid the uuid of the player to cache
     * @param inventory the player's inventory
     * @param hash the hash of the player's inventory
     */
    public void cacheInventory(UUID uuid, String inventory, String hash) {
        if (cachedInventories.containsKey(uuid)) {
            cachedInventories.replace(uuid, inventory);
            inventoryHash.replace(uuid, hash);
            return;
        }
        cachedInventories.put(uuid, hash);
        inventoryHash.put(uuid, hash);
    }

    /**
     * Get the inventory for a player
     *
     * @param uuid the uuid of the player
     * @return the inventory of the player. Defaults to a blank string if none is present
     */
    public String getInventory(UUID uuid) {
        return cachedInventories.getOrDefault(uuid, "");
    }

    /**
     * Remove an inventory hash
     *
     * @param uuid the uuid who's hash to remove
     */
    public void removeHash(UUID uuid) {
        if (cachedInventories.containsKey(uuid)) {
            cachedInventories.remove(uuid);
            inventoryHash.remove(uuid);
        }
    }

    /**
     * Get the hash of a player's inventory
     *
     * @param uuid the uuid of the player
     * @return the player's inventory hash. Empty if none.
     */
    public String getHash(UUID uuid) {
        return inventoryHash.getOrDefault(uuid, "");
    }
}
