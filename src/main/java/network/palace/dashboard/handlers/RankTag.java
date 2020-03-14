package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.dashboard.chat.ChatColor;

import java.util.List;

@AllArgsConstructor
public enum RankTag {
    /* Media Team */
    DESIGNER("Resource Pack Designer", "D", ChatColor.BLUE, 7),
    /* Guide Program */
    GUIDE("Guide Team", "G", ChatColor.DARK_GREEN, 6),
    /* Sponsor Tiers */
    SPONSOR_OBSIDIAN("Obsidian Tier Sponsor", "S", ChatColor.DARK_PURPLE, 5),
    SPONSOR_EMERALD("Emerald Tier Sponsor", "S", ChatColor.GREEN, 4),
    SPONSOR_DIAMOND("Diamond Tier Sponsor", "S", ChatColor.AQUA, 3),
    SPONSOR_LAPIS("Lapis Tier Sponsor", "S", ChatColor.BLUE, 2),
    SPONSOR_GOLD("Gold Tier Sponsor", "S", ChatColor.YELLOW, 1),
    SPONSOR_IRON("Iron Tier Sponsor", "S", ChatColor.GRAY, 0);

    @Getter private String name;
    private String tag;
    @Getter private ChatColor color;
    @Getter private int id;

    public String getTag() {
        return ChatColor.WHITE + "[" + color + ChatColor.BOLD + tag + ChatColor.WHITE + "] ";
    }

    /**
     * Get tag object from a string
     *
     * @param name tag name in string
     * @return tag object
     */
    public static RankTag fromString(String name) {
        if (name == null || name.isEmpty()) return null;

        for (RankTag tier : RankTag.values()) {
            if (tier.getDBName().equalsIgnoreCase(name)) return tier;
        }
        return null;
    }

    public String getDBName() {
        return name().toLowerCase();
    }

    public String getScoreboardTag() {
        return " " + ChatColor.WHITE + "[" + color + "S" + ChatColor.WHITE + "]";
    }

    public static String format(List<RankTag> tags) {
        tags.sort((rankTag, t1) -> t1.id - rankTag.id);
        StringBuilder s = new StringBuilder();
        for (RankTag tag : tags) {
            s.append(tag.getTag());
        }
        return s.toString();
    }
}
