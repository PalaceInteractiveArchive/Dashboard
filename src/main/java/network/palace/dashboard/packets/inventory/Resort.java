package network.palace.dashboard.packets.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 6/10/2017
 */
@AllArgsConstructor
public enum Resort {
    WDW(0, new String[]{"all"}), USO(2, new String[]{"uso", "usoevent"});
    @Getter private int id;
    @Getter private String[] server;

    public String getName() {
        return name().toLowerCase();
    }

    public static Resort fromId(int id) {
        for (Resort type : values()) {
            if (type.getId() == id) return type;
        }
        return WDW;
    }

    public static Resort fromServer(String name) {
        for (Resort type : values()) {
            for (String s : type.getServer()) {
                if (s.equalsIgnoreCase(name)) {
                    return type;
                }
            }
        }
        return WDW;
    }
}
