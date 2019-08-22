package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.palace.dashboard.packets.inventory.Resort;

/**
 * Created by Marc on 6/10/17.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResortInventory {
    private Resort resort;
    private String backpackJSON = "";
    private String backpackHash = "";
    private String dbBackpackHash = "";
    private int backpackSize;

    private String lockerJSON = "";
    private String lockerHash = "";
    private String dbLockerHash = "";
    private int lockerSize;

    private String baseJSON = "";
    private String baseHash = "";
    private String dbBaseHash = "";

    private String buildJSON = "";
    private String buildHash = "";
    private String dbBuildHash = "";

    /**
     * Check if all JSON entries are empty (meaning no data is here, not even empty inventories)
     *
     * @return true if four JSON entries are empty, otherwise false
     */
    public boolean isEmpty() {
        return backpackJSON.isEmpty() && lockerJSON.isEmpty() && baseJSON.isEmpty() && buildJSON.isEmpty();
    }
}
