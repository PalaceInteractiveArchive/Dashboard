package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Rank {

    MANAGER("Manager", ChatColor.RED + "Manager ", ChatColor.RED, ChatColor.YELLOW, true, 12),
    ADMIN("Admin", ChatColor.RED + "Admin ", ChatColor.RED, ChatColor.YELLOW, true, 12),
    DEVELOPER("Developer", ChatColor.GOLD + "Developer ", ChatColor.GOLD, ChatColor.YELLOW, true, 12),
    SRMOD("Sr Mod", ChatColor.YELLOW + "Sr Mod ", ChatColor.YELLOW, ChatColor.GREEN, true, 11),
    ARCHITECT("Architect", ChatColor.YELLOW + "Architect ", ChatColor.YELLOW, ChatColor.GREEN, true, 11),
    BUILDER("Builder", ChatColor.GREEN + "Builder ", ChatColor.GREEN, ChatColor.GREEN, true, 10),
    MOD("Mod", ChatColor.GREEN + "Mod ", ChatColor.GREEN, ChatColor.GREEN, true, 10),
    TRAINEEBUILD("Trainee", ChatColor.DARK_GREEN + "Trainee ", ChatColor.DARK_GREEN, ChatColor.DARK_GREEN, false, 9),
    TRAINEE("Trainee", ChatColor.DARK_GREEN + "Trainee ", ChatColor.DARK_GREEN, ChatColor.DARK_GREEN, false, 8),
    CHARACTER("Character", ChatColor.BLUE + "Character ", ChatColor.BLUE, ChatColor.BLUE, false, 7),
    SPECIALGUEST("Special Guest", ChatColor.DARK_PURPLE + "SG ", ChatColor.DARK_PURPLE, ChatColor.WHITE, false, 6),
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
        if (name == null) return SETTLER;
        String rankName = name.replaceAll(" ", "");

        for (Rank rank : Rank.values()) {
            if (rank.getDBName().equalsIgnoreCase(rankName)) return rank;
        }
        return SETTLER;
    }

    public String getDBName() {
        if (this.equals(TRAINEEBUILD)) {
            return "traineebuild";
        }
        return name.toLowerCase().replaceAll(" ", "");
    }

    /**
     * Get the formatted name of a rank
     *
     * @return the rank name with any additional formatting that should exist
     */
    public String getFormattedName() {
        String bold = getRankId() >= Rank.TRAINEE.getRankId() ? "" + ChatColor.BOLD : "";
        return getTagColor() + bold + getName();
    }
}
