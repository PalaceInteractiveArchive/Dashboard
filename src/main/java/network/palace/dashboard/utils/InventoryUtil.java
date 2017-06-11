package network.palace.dashboard.utils;

import network.palace.dashboard.handlers.InventoryCache;
import network.palace.dashboard.handlers.ResortInventory;
import network.palace.dashboard.packets.inventory.PacketInventoryContent;
import network.palace.dashboard.packets.inventory.Resort;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Innectic
 * @since 6/10/2017
 */
public class InventoryUtil {
    private Map<UUID, InventoryCache> cachedInventories = new HashMap<>();

    /**
     * Cache a player's inventory
     *
     * @param uuid      the uuid of the player to cache
     * @param inventory the player's inventory
     */
    public void cacheInventory(UUID uuid, PacketInventoryContent packet) {
        if (cachedInventories.containsKey(uuid)) {
            cachedInventories.get(uuid).setInventory(packet.getResort(), new ResortInventory(packet.getBackpackJson(),
                    packet.getBackpackHash(), packet.getLockerJson(), packet.getLockerHash(), packet.getHotbarJson(),
                    packet.getHotbarHash()));
            return;
        }
        HashMap<Resort, ResortInventory> map = new HashMap<>();
        map.put(packet.getResort(), new ResortInventory(packet.getBackpackJson(), packet.getBackpackHash(),
                packet.getLockerJson(), packet.getLockerHash(), packet.getHotbarJson(), packet.getHotbarHash()));
        InventoryCache cache = new InventoryCache(uuid, map);
        cachedInventories.put(uuid, cache);
    }

    /**
     * Get the inventory for a player
     *
     * @param uuid the uuid of the player
     * @return the inventory of the player. Defaults to a blank string if none is present
     */
    public ResortInventory getInventory(UUID uuid, Resort resort) {
        InventoryCache cache = cachedInventories.getOrDefault(uuid, new InventoryCache(uuid, blankMap(resort)));
        ResortInventory inv = cache.getResorts().get(resort);
        if (inv == null) {
            return new ResortInventory();
        }
        return inv;
    }

    private HashMap<Resort, ResortInventory> blankMap(Resort resort) {
        HashMap<Resort, ResortInventory> map = new HashMap<>();
        map.put(resort, new ResortInventory());
        return map;
    }

    /**
     * Remove an inventory hash
     *
     * @param uuid the uuid who's hash to remove
     */
    public void removeHash(UUID uuid) {
        cachedInventories.remove(uuid);
    }
}
