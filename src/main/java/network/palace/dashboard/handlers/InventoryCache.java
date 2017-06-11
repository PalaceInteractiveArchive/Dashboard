package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.dashboard.packets.inventory.Resort;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Marc on 6/10/17.
 */
@AllArgsConstructor
public class InventoryCache {
    @Getter private UUID uuid;
    @Getter private HashMap<Resort, ResortInventory> resorts = new HashMap<>();

    public void setInventory(Resort resort, ResortInventory inv) {
        resorts.put(resort, inv);
    }
}
