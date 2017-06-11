package network.palace.dashboard.packets.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 6/10/2017
 */
@AllArgsConstructor
public enum Resort {
    WDW(0, "all"), DLR(1, "dlr"), USO(2, "uso");
    @Getter private int id;
    @Getter private String server;

    public static Resort fromId(int id) {
        for (Resort type : values()) {
            if (type.getId() == id) return type;
        }
        return WDW;
    }

    public static Resort fromServer(String name) {
        for (Resort type : values()) {
            if (type.getServer().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return WDW;
    }
}
