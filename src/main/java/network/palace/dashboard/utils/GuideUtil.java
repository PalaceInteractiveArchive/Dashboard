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
    private HashMap<UUID, String> announcementRequests = new HashMap<>();

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
                if (tp.hasMentions() && tp.hasTag(RankTag.GUIDE)) {
                    tp.mention();
                }
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
        } else {
            lastRequest.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage(ChatColor.GREEN + "Your help request has been sent!");
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
                (System.currentTimeMillis() - lastRequest.remove(target.getUniqueId()) >= 10 * 60 * 1000)) {
            player.sendMessage(ChatColor.RED + "That player hasn't submitted a help request recently!");
            return;
        }
        BaseComponent[] components = new ComponentBuilder("[").color(ChatColor.WHITE)
                .append("HELP").color(ChatColor.GREEN).append("] ").color(ChatColor.WHITE)
                .append(player.getUsername()).color(player.getRank().getTagColor())
                .append(" accepted ").color(ChatColor.AQUA)
                .append(target.getUsername() + "'s ").color(target.getRank().getTagColor())
                .append("help request").color(ChatColor.AQUA).create();
        Launcher.getDashboard().getOnlinePlayers().stream()
                .filter(tp -> tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() || tp.hasTag(RankTag.GUIDE))
                .forEach(tp -> tp.sendMessage(components));

        Rank rank = player.getRank();
        target.sendMessage(new ComponentBuilder(rank.getName()).color(rank.getTagColor()).bold(true)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getUsername() + " "))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to start your message with " + player.getUsername()).color(ChatColor.GREEN).create()))
                .append(" " + player.getUsername()).bold(false)
                .append(" has accepted your help request. Contact them by typing ").color(ChatColor.AQUA)
                .append("/msg " + player.getUsername() + " [Your Message]").color(ChatColor.YELLOW)
                .append(" (or click on this message)").color(ChatColor.GREEN).create());
        Launcher.getDashboard().getMongoHandler().logHelpRequest(target.getUniqueId(), player.getUniqueId());
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

    /**
     * Submit an announcement request
     *
     * @param player       the guide/trainee submitting the help request
     * @param announcement the announcement to be broadcasted
     */
    public void sendAnnouncementRequest(Player player, String announcement) {
        String firstMessage = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                ChatColor.GREEN + player.getUsername() + " wants to send the announcement: " + announcement;
        BaseComponent[] components = new ComponentBuilder("Accept Request").color(ChatColor.DARK_GREEN).italic(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to accept this announcement request").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gannounce accept " + player.getUsername()))
                .append(" - ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GREEN)
                .append("Decline Request").color(ChatColor.RED).italic(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to decline this announcement request").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gannounce decline " + player.getUsername())).create();
        boolean staff = false;
        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            if (tp.getRank().getRankId() >= Rank.MOD.getRankId()) {
                staff = true;
                tp.sendMessage(firstMessage);
                tp.sendMessage(components);
            }
        }
        if (!staff) {
            player.sendMessage(new ComponentBuilder("Unfortunately, there aren't any staff online to accept this announcement request.").color(ChatColor.AQUA).create());
        } else {
            announcementRequests.put(player.getUniqueId(), announcement);
        }
    }

    /**
     * Accept an announcement request submitted by a guide/trainee
     *
     * @param player   the staff member accepting the request
     * @param username the guide/trainee who submitted the request
     */
    public void acceptAnnouncementRequest(Player player, String username) {
        Dashboard dashboard = Launcher.getDashboard();
        Player tp = dashboard.getPlayer(username);
        if (tp == null || !announcementRequests.containsKey(tp.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "That player hasn't submitted an announcement request recenty!");
            return;
        }
        for (Player p : dashboard.getOnlinePlayers()) {
            if (p.getRank().getRankId() >= Rank.MOD.getRankId()) {
                p.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                        ChatColor.GREEN + player.getUsername() + ChatColor.AQUA + " accepted " + ChatColor.GREEN +
                        tp.getUsername() + "'s " + ChatColor.AQUA + "announcement request");
            }
        }
        tp.sendMessage(player.getRank().getTagColor() + player.getUsername() + ChatColor.AQUA +
                " has accepted your announcement request.");

        String message = announcementRequests.remove(tp.getUniqueId());
        String msg = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" + ChatColor.WHITE + "] " +
                ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);
        String staff = ChatColor.WHITE + "[" + ChatColor.AQUA + tp.getUsername() + ChatColor.WHITE + "] " +
                ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);
        for (Player p : dashboard.getOnlinePlayers()) {
            if (dashboard.getPlayer(p.getUniqueId()).getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                p.sendMessage(staff);
            } else {
                p.sendMessage(msg);
            }
        }
    }

    /**
     * Decline an announcement request submitted by a guide/trainee
     *
     * @param player   the staff member declining the request
     * @param username the guide/trainee who submitted the request
     */
    public void declineAnnouncementRequest(Player player, String username) {
        Player tp = Launcher.getDashboard().getPlayer(username);
        if (tp == null || announcementRequests.remove(tp.getUniqueId()) == null) {
            player.sendMessage(ChatColor.RED + "That player hasn't submitted an announcement request recenty!");
            return;
        }
        for (Player p : Launcher.getDashboard().getOnlinePlayers()) {
            if (p.getRank().getRankId() >= Rank.MOD.getRankId()) {
                p.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                        ChatColor.GREEN + player.getUsername() + ChatColor.AQUA + " declined " + ChatColor.GREEN +
                        tp.getUsername() + "'s " + ChatColor.AQUA + "announcement request");
            }
        }
        tp.sendMessage(player.getRank().getTagColor() + player.getUsername() + ChatColor.AQUA +
                " has declined your announcement request.");
    }

    public boolean overloaded() {
        int count = 0;
        long current = System.currentTimeMillis();
        for (long sent : lastRequest.values()) {
            if ((current - sent) <= 30 * 1000) count++;
            if (count >= 5) break;
        }
        return count >= 5;
    }
}
