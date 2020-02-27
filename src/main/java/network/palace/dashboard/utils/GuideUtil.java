package network.palace.dashboard.utils;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.*;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.RankTag;

import java.util.HashMap;
import java.util.UUID;

public class GuideUtil {
    private HashMap<UUID, Long> lastRequest = new HashMap<>();

    public boolean canSubmitHelpRequest(Player player) {
        return !lastRequest.containsKey(player.getUniqueId()) ||
                System.currentTimeMillis() - lastRequest.get(player.getUniqueId()) >= 30 * 1000;
    }

    public void sendHelpRequest(Player player, String request) {
        lastRequest.put(player.getUniqueId(), System.currentTimeMillis());
        BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE)
                .append("HELP").color(ChatColor.GREEN).append("] ").color(ChatColor.WHITE)
                .append(player.getUsername()).color(player.getRank().getTagColor())
                .append(" submitted a help request: ").color(ChatColor.AQUA)
                .append(request + "\n").color(ChatColor.GREEN)
                .append("Accept Request").color(ChatColor.DARK_GREEN).italic(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to accept this help request!").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/h accept " + player.getUsername())).create();
        Launcher.getDashboard().getOnlinePlayers().stream()
                .filter(tp -> tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() || tp.hasTag(RankTag.GUIDE))
                .forEach(tp -> tp.sendMessage(components));
    }
}
