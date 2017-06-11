package network.palace.dashboard.handlers;

import network.palace.dashboard.packets.inventory.Resort;

import java.util.HashMap;

/**
 * Created by Marc on 6/10/17.
 */
public class InventoryUpdate {
    private HashMap<Resort, HashMap<String, String>> resorts = new HashMap<>();

    public void setValue(Resort resort, String key, String value) {
        HashMap<String, String> res = resorts.remove(resort);
        if (res == null) {
            res = new HashMap<>();
        }
        res.put(key, value);
        resorts.put(resort, res);
    }

    public HashMap<String, String> getChanges(Resort resort) {
        return resorts.get(resort);
    }

    public boolean shouldUpdate() {
        return !resorts.isEmpty();
    }

    public HashMap<Resort, HashMap<String, String>> getMap() {
        return resorts;
    }
}
