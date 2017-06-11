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
    @Getter private Resort resort;
    @Getter @Setter private String backpackJSON;
    @Getter @Setter private String backpackHash;
    @Getter @Setter private String sqlBackpackHash;
    @Getter @Setter private int backpackSize;

    @Getter @Setter private String lockerJSON;
    @Getter @Setter private String lockerHash;
    @Getter @Setter private String sqlLockerHash;
    @Getter @Setter private int lockerSize;

    @Getter @Setter private String hotbarJSON;
    @Getter @Setter private String hotbarHash;
    @Getter @Setter private String sqlHotbarHash;
}
