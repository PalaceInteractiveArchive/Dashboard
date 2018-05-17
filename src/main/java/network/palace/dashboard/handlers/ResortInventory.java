package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.palace.dashboard.packets.inventory.Resort;

/**
 * Created by Marc on 6/10/17.
 */
@AllArgsConstructor
@NoArgsConstructor
public class ResortInventory {
    @Getter @Setter private Resort resort;
    @Getter @Setter private String backpackJSON = "";
    @Getter @Setter private String backpackHash = "";
    @Getter @Setter private String dbBackpackHash = "";
    @Getter @Setter private int backpackSize;

    @Getter @Setter private String lockerJSON = "";
    @Getter @Setter private String lockerHash = "";
    @Getter @Setter private String dbLockerHash = "";
    @Getter @Setter private int lockerSize;

    @Getter @Setter private String hotbarJSON = "";
    @Getter @Setter private String hotbarHash = "";
    @Getter @Setter private String dbHotbarHash = "";

    /**
     * Check if all JSON entries are empty (meaning no data is here, not even empty inventories)
     *
     * @return true if three JSON entries are empty, otherwise false
     */
    public boolean isEmpty() {
        return backpackJSON.isEmpty() && lockerJSON.isEmpty() && hotbarJSON.isEmpty();
    }
}
