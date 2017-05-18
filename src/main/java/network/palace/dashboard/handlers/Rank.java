package network.palace.dashboard.handlers;

/**
 * Created by Marc on 7/15/16
 */
public enum Rank {
    EMPRESS("Empress", ChatColor.RED, ChatColor.YELLOW, 11),
    EMPEROR("Emperor", ChatColor.RED, ChatColor.YELLOW, 11),
    WIZARD("Wizard", ChatColor.GOLD, ChatColor.YELLOW, 11),
    PALADIN("Paladin", ChatColor.YELLOW, ChatColor.GREEN, 10),
    ARCHITECT("Architect", ChatColor.GREEN, ChatColor.GREEN, 9),
    KNIGHT("Knight", ChatColor.GREEN, ChatColor.GREEN, 9),
    SQUIRE("Squire", ChatColor.DARK_GREEN, ChatColor.DARK_GREEN, 8),
    CHARACTER("Character", ChatColor.BLUE, ChatColor.BLUE, 7),
    SPECIALGUEST("Special Guest", ChatColor.DARK_PURPLE, ChatColor.WHITE, 6),
    MCPROHOSTING("MCProHosting", ChatColor.RED, ChatColor.WHITE, 6),
    HONORABLE("Honorable", ChatColor.LIGHT_PURPLE, ChatColor.WHITE, 5),
    MAJESTIC("Majestic", ChatColor.DARK_PURPLE, ChatColor.WHITE, 4),
    NOBLE("Noble", ChatColor.BLUE, ChatColor.WHITE, 3),
    DWELLER("Dweller", ChatColor.AQUA, ChatColor.WHITE, 2),
    SHAREHOLDER("Shareholder", ChatColor.LIGHT_PURPLE, ChatColor.WHITE, 3),
    DVCMEMBER("DVC", ChatColor.AQUA, ChatColor.WHITE, 2),
    SETTLER("Settler", ChatColor.DARK_AQUA, ChatColor.WHITE, 1);

    private String name;
    private ChatColor tagColor;
    private ChatColor chatColor;
    private int rankId;

    Rank(String name, ChatColor tagColor, ChatColor chatColor, int rankId) {
        this.name = name;
        this.tagColor = tagColor;
        this.chatColor = chatColor;
        this.rankId = rankId;
    }

    public int getRankId() {
        return rankId;
    }

    public static Rank fromString(String string) {
        String rankName = string.toLowerCase();
        switch (rankName) {
            case "empress":
                return EMPRESS;
            case "emperor":
                return EMPEROR;
            case "wizard":
                return WIZARD;
            case "paladin":
                return PALADIN;
            case "architect":
                return ARCHITECT;
            case "knight":
                return KNIGHT;
            case "squire":
                return SQUIRE;
            case "character":
                return CHARACTER;
            case "specialguest":
                return SPECIALGUEST;
            case "mcprohosting":
                return MCPROHOSTING;
            case "honorable":
                return HONORABLE;
            case "majestic":
                return MAJESTIC;
            case "noble":
                return NOBLE;
            case "dweller":
                return DWELLER;
            case "shareholder":
                return SHAREHOLDER;
            case "dvc":
                return DVCMEMBER;
            default:
                return SETTLER;
        }
    }

    public String getName() {
        return name;
    }

    public String getSqlName() {
        return name.toLowerCase().replaceAll(" ", "");
    }

    public String getNameWithBrackets() {
        return ChatColor.WHITE + "[" + getTagColor() + getName() + ChatColor.WHITE + "]";
    }

    public String getNameWithFormatting() {
        String bolded = getRankId() >= 8 ? ChatColor.BOLD.toString() : "";
        return getTagColor() + bolded + getName();
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public ChatColor getTagColor() {
        return tagColor;
    }
}