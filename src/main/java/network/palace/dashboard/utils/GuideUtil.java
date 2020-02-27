package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.*;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.RankTag;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class GuideUtil {
    private HashMap<UUID, Long> lastRequest = new HashMap<>();

    /**
     * Check whether a player can submit a help request
     *
     * @param player the player
     * @return true if the player hasn't submitted an unanswered request in the last 30 seconds
     */
    public boolean canSubmitHelpRequest(Player player) {
        return !lastRequest.containsKey(player.getUniqueId()) ||
                System.currentTimeMillis() - lastRequest.get(player.getUniqueId()) >= 30 * 1000;
    }

    /**
     * Submit a help request to online staff and Guides
     *
     * @param player  the player submitting the help request
     * @param request the request being submitted
     */
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
        boolean staff = false;
        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            if (tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() || tp.hasTag(RankTag.GUIDE)) {
                staff = true;
                tp.sendMessage(components);
            }
        }
        if (!staff) {
            player.sendMessage(new ComponentBuilder("Unfortunately, there isn't anyone online right now to help with your request. In the meantime, you could ask for help on our ").color(ChatColor.AQUA)
                    .append("Discord server.").color(ChatColor.BLUE)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to visit ").color(ChatColor.GREEN)
                                    .append("https://palnet.us/Discord").color(ChatColor.YELLOW).create()))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://palnet.us/Discord")).create());
        }
    }

    /**
     * Accept the help request submitted by the target player
     *
     * @param player the staff/guide accepting the request
     * @param target the player who submitted the request
     */
    public void acceptHelpRequest(Player player, Player target) {
        if (!lastRequest.containsKey(target.getUniqueId()) ||
                (System.currentTimeMillis() - lastRequest.remove(player.getUniqueId()) >= 10 * 60 * 1000)) {
            player.sendMessage(ChatColor.RED + "That player hasn't submitted a help request recently!");
            return;
        }
        Rank rank = player.getRank();
        target.sendMessage(new ComponentBuilder(rank.getName()).color(rank.getTagColor()).bold(true)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getUsername() + " "))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to start your message with " + player.getUsername()).color(ChatColor.GREEN).create()))
                .append(" " + player.getUsername()).bold(false)
                .append(" has accepted your help request. Contact them by typing ").color(ChatColor.AQUA)
                .append("/msg " + player.getUsername() + " [Your Message]").color(ChatColor.YELLOW)
                .append(" (or click on this message)").color(ChatColor.GREEN).create());
    }

    /**
     * Teleport the player to the target player, across servers if necessary
     *
     * @param player the staff/guide teleporting
     * @param target the player being teleported to
     */
    public void teleport(Player player, Player target) {
        String targetServer = target.getServer();
        if (player.getServer().equals(targetServer)) {
            player.sendMessage(ChatColor.GREEN + "You're already on the same server as this player! Teleporting you to " + target.getUsername() + "...");
            player.chat("/tp " + target.getUsername());
            return;
        }
        Dashboard dashboard = Launcher.getDashboard();
        player.sendMessage(ChatColor.GREEN + "Sending you to " + targetServer + "...");
        dashboard.getServerUtil().sendPlayer(player, targetServer);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            int counts = 0;
            boolean lastRun = false;

            @Override
            public void run() {
                try {
                    if (lastRun) {
                        cancel();
                        player.sendMessage(ChatColor.GREEN + "Teleporting you to " + target.getUsername() + "...");
                        player.chat("/tp " + target.getUsername());
                        return;
                    }
                    if (player.getServer().equals(targetServer)) {
                        lastRun = true;
                    }
                    if (counts++ >= 10 && !lastRun) {
                        cancel();
                        player.sendMessage(ChatColor.RED + "Request timed out!\nThere was an issue sending you to " +
                                targetServer + ", so your teleport request couldn't be completed.");
                    }
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "There was an issue handling this request!");
                    cancel();
                }
            }
        }, 500, 500);
    }
}
