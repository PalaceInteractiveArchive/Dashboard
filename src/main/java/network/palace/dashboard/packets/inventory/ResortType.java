package network.palace.dashboard.packets.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 6/10/2017
 */
@AllArgsConstructor
public enum ResortType {
    WDW(0), DLR(1), USO(2);
    @Getter private int id;

    public static ResortType fromId(int id) {
        for (ResortType type : values()) {
            if (type.getId() == id) return type;
        }
        return WDW;
    }
}
