package network.palace.dashboard.handlers;

import network.palace.dashboard.packets.inventory.Resort;

import java.util.HashMap;

/**
 * Created by Marc on 6/10/17.
 */
public class InventoryUpdate {
    private HashMap<Resort, UpdateData> resorts = new HashMap<>();

    public void setData(Resort resort, UpdateData data) {
        if (resorts.containsKey(resort)) {
            return;
        }
        resorts.put(resort, data);
    }

    public UpdateData getData(Resort resort) {
        return resorts.get(resort);
    }

    public boolean shouldUpdate() {
        return !resorts.isEmpty();
    }

    public HashMap<Resort, UpdateData> getMap() {
        return resorts;
    }
}
