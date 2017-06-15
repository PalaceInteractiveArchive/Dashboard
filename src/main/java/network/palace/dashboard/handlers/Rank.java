package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Rank {

    EMPRESS("Empress", ChatColor.RED + "Empress ", ChatColor.RED, ChatColor.YELLOW, true, 11),
    EMPEROR("Emperor", ChatColor.RED + "Emperor ", ChatColor.RED, ChatColor.YELLOW, true, 11),
    WIZARD("Wizard", ChatColor.GOLD + "Wizard ", ChatColor.GOLD, ChatColor.YELLOW, true, 11),
    PALADIN("Paladin", ChatColor.YELLOW + "Paladin ", ChatColor.YELLOW, ChatColor.GREEN, true, 10),
    ARCHITECT("Architect", ChatColor.GREEN + "Architect ", ChatColor.GREEN, ChatColor.GREEN, true, 9),
    KNIGHT("Knight", ChatColor.GREEN + "Knight ", ChatColor.GREEN, ChatColor.GREEN, true, 9),
    SQUIRE("Squire", ChatColor.DARK_GREEN + "Squire ", ChatColor.DARK_GREEN, ChatColor.DARK_GREEN, false, 8),
    CHARACTER("Character", ChatColor.BLUE + "Character ", ChatColor.BLUE, ChatColor.BLUE, false, 7),
    SPECIALGUEST("Special Guest", ChatColor.DARK_PURPLE + "SG ", ChatColor.DARK_PURPLE, ChatColor.WHITE, false, 6),
    MCPROHOSTING("MCProHosting", ChatColor.RED + "MCPro ", ChatColor.RED, ChatColor.WHITE, false, 6),
    HONORABLE("Honorable", ChatColor.LIGHT_PURPLE + "Honorable ", ChatColor.LIGHT_PURPLE, ChatColor.WHITE, false, 5),
    MAJESTIC("Majestic", ChatColor.DARK_PURPLE + "Majestic ", ChatColor.DARK_PURPLE, ChatColor.WHITE, false, 4),
    NOBLE("Noble", ChatColor.BLUE + "Noble ", ChatColor.BLUE, ChatColor.WHITE, false, 3),
    SHAREHOLDER("Shareholder", ChatColor.LIGHT_PURPLE + "Shareholder ", ChatColor.LIGHT_PURPLE, ChatColor.WHITE, false, 3),
    DWELLER("Dweller", ChatColor.AQUA + "Dweller ", ChatColor.AQUA, ChatColor.WHITE, false, 2),
    DVCMEMBER("DVC", ChatColor.AQUA + "DVC ", ChatColor.AQUA, ChatColor.WHITE, false, 2),
    SETTLER("Settler", ChatColor.GRAY + "", ChatColor.DARK_AQUA, ChatColor.WHITE, false, 1);

    @Getter private String name;
    @Getter private String scoreboardName;
    @Getter private ChatColor tagColor;
    @Getter private ChatColor chatColor;
    @Getter private boolean isOp;
    @Getter private int rankId;

    /**
     * Get rank object from a string
     *
     * @param name rank name in string
     * @return rank object
     */
    public static Rank fromString(String name) {
        for (Rank rank : Rank.values()) {
            if (rank.getName().replaceAll(" ", "").equalsIgnoreCase(name)) return rank;
        }
        return SETTLER;
    }

    public String getSqlName() {
        return name.toLowerCase().replaceAll(" ", "");
    }

    @Deprecated
    public String getNameWithBrackets() {
        return getFormattedName();
    }

    /**
     * Get the formatted name of a rank
     *
     * @return the rank name with any additional formatting that should exist
     */
    public String getFormattedName() {
        String bold = getRankId() >= Rank.SQUIRE.getRankId() ? "" + ChatColor.BOLD : "";
        return getTagColor() + bold + getName();
    }
}
