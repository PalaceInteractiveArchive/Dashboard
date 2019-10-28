package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ClickEvent;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.chat.HoverEvent;

import java.util.*;

/**
 * Created by Marc on 8/22/16
 */
public class FriendUtil {

    public static void teleportPlayer(Player player, Player target) {
        Dashboard dashboard = Launcher.getDashboard();
        if (target == null) {
            return;
        }
        if (player.getServer().equals(target.getServer())) {
            player.sendMessage(ChatColor.RED + "You're already on the same server as " + ChatColor.AQUA +
                    target.getUsername() + "!");
            return;
        }
        try {
            dashboard.getServerUtil().sendPlayer(player, target.getServer());
            player.sendMessage(ChatColor.BLUE + "You connected to the server " + ChatColor.GREEN + target.getUsername() +
                    " " + ChatColor.BLUE + "is on! (" + target.getServer() + ")");
        } catch (Exception ignored) {
        }
    }

    public static void listFriends(final Player player, int page) {
        Dashboard dashboard = Launcher.getDashboard();
        HashMap<UUID, String> friends = player.getFriends();
        if (friends.isEmpty()) {
            player.sendMessage(ChatColor.RED + "\nType /friend add [Player] to add someone\n");
            return;
        }
        int listSize = friends.size();
        int maxPage = (int) Math.ceil((double) friends.size() / 8);
        if (page > maxPage) {
            page = maxPage;
        }
        int startAmount = 8 * (page - 1);
        int endAmount;
        if (maxPage > 1) {
            if (page < maxPage) {
                endAmount = (8 * page);
            } else {
                endAmount = listSize;
            }
        } else {
            endAmount = listSize;
        }
        List<String> currentFriends = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : friends.entrySet()) {
            Player tp = dashboard.getPlayer(entry.getKey());
            if (tp == null) {
                currentFriends.add(entry.getValue() == null ? "unknown" : entry.getValue());
            } else {
                String sname = dashboard.getServer(tp.getServer()).getServerType();
                if (sname.startsWith("New")) {
                    sname = sname.replaceAll("New", "");
                }
                currentFriends.add(entry.getValue() + ":" + sname);
            }
        }
        currentFriends.sort((o1, o2) -> {
            boolean c1 = o1.contains(":");
            boolean c2 = o2.contains(":");
            if (c1 && !c2) {
                return -1;
            } else if (!c1 && c2) {
                return 1;
            } else {
                return o1.compareTo(o2);
            }
        });
        ComponentBuilder message = new ComponentBuilder("\nFriend List ").color(ChatColor.YELLOW)
                .append("[Page " + page + " of " + maxPage + "]").color(ChatColor.GREEN);
        for (String str : currentFriends.subList(startAmount, endAmount)) {
            String[] list = str.split(":");
            String user = list[0];
            if (list.length > 1) {
                String server = list[1];
                message.append("\n- ").color(ChatColor.AQUA).append(user).color(ChatColor.GREEN)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to join ")
                                .color(ChatColor.GREEN).append(server + "!").color(ChatColor.YELLOW)
                                .create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/friend tp " + server));
            } else {
                message.append("\n- ").color(ChatColor.AQUA).append(user).color(ChatColor.RED)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("This player is offline!")
                                .color(ChatColor.RED).create()));
            }
        }
        player.sendMessage(message.create());
    }

    public static void listRequests(final Player player) {
        HashMap<UUID, String> requests = player.getRequests();
        if (requests.isEmpty()) {
            player.sendMessage(ChatColor.RED + "\nYou currently have no Friend Requests!\n");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Request List:");
        for (String s : requests.values()) {
            player.sendMessage(new ComponentBuilder("- ").color(ChatColor.AQUA).append(s)
                    .color(ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to Accept the Request!").color(ChatColor.GREEN).create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + s)).create());
        }
        player.sendMessage(new ComponentBuilder(" ").create());
    }

    public static void addFriend(Player player, String name) {
        Dashboard dashboard = Launcher.getDashboard();
        if (name.equalsIgnoreCase(player.getUsername())) {
            player.sendMessage(ChatColor.RED + "You can't be your own friend, sorry!");
            return;
        }
        HashMap<UUID, String> friendList = player.getFriends();
        for (String s : friendList.values()) {
            if (s.equalsIgnoreCase(name)) {
                player.sendMessage(ChatColor.RED + "That player is already on your Friend List!");
                return;
            }
        }
        Player tp = dashboard.getPlayer(name);
        if (tp == null) {
            try {
                UUID tuuid = dashboard.getMongoHandler().usernameToUUID(name);
                HashMap<UUID, String> requests = dashboard.getMongoHandler().getRequestList(tuuid);
                if (requests.containsKey(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You have already sent this player a Friend Request!");
                    return;
                }
                if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                    if (!dashboard.getMongoHandler().getFriendRequestToggle(tuuid)) {
                        player.sendMessage(ChatColor.RED + "That player has Friend Requests toggled off!");
                        return;
                    }
                }
                player.sendMessage(ChatColor.YELLOW + "You have sent " + ChatColor.AQUA + name + ChatColor.YELLOW +
                        " a Friend Request!");
                /* Add request to database */
                dashboard.getMongoHandler().addFriendRequest(player.getUniqueId(), tuuid);
                dashboard.getMongoHandler().logActivity(player.getUniqueId(), "Send Friend Request", name);
            } catch (Exception ignored) {
                player.sendMessage(ChatColor.RED + "That player could not be found!");
            }
            return;
        }
        if (tp.getRequests().containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have already sent this player a Friend Request!");
            return;
        }
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
            if (tp.hasFriendToggledOff()) {
                player.sendMessage(ChatColor.RED + "That player has Friend Requests toggled off!");
                return;
            }
        }
        tp.getRequests().put(player.getUniqueId(), player.getUsername());
        player.sendMessage(ChatColor.YELLOW + "You have sent " + ChatColor.AQUA + tp.getUsername() + ChatColor.YELLOW +
                " a Friend Request!");

        tp.sendMessage(new ComponentBuilder("\n" + player.getUsername()).color(ChatColor.GREEN)
                .append(" has sent you a Friend Request!").color(ChatColor.YELLOW).create());
        tp.sendMessage(new ComponentBuilder("Click to Accept").color(ChatColor.GREEN).bold(true).event(new
                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept " + player.getUsername())).append(" or ",
                ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).append("Click to Deny\n")
                .color(ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/friend deny " + player.getUsername())).create());

        /* Add request to database */
        dashboard.getMongoHandler().addFriendRequest(player.getUniqueId(), tp.getUniqueId());
        dashboard.getMongoHandler().logActivity(player.getUniqueId(), "Send Friend Request", name);
    }

    public static void removeFriend(Player player, String name) {
        Dashboard dashboard = Launcher.getDashboard();
        Player tp = dashboard.getPlayer(name);
        if (tp == null) {
            try {
                UUID tuuid = dashboard.getMongoHandler().usernameToUUID(name);
                if (!player.getFriends().containsKey(tuuid)) {
                    player.sendMessage(ChatColor.RED + "That player isn't on your Friend List!");
                    return;
                }
                player.getFriends().remove(tuuid);
                player.sendMessage(ChatColor.RED + "You removed " + ChatColor.AQUA + name + ChatColor.RED +
                        " from your Friend List!");
                dashboard.getMongoHandler().removeFriend(player.getUniqueId(), tuuid);
            } catch (Exception ignored) {
                player.sendMessage(ChatColor.RED + "That player could not be found!");
            }
            dashboard.getMongoHandler().logActivity(player.getUniqueId(), "Remove Friend", name);
            return;
        }
        if (!player.getFriends().containsKey(tp.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "That player isn't on your Friend List!");
            return;
        }
        player.getFriends().remove(tp.getUniqueId());
        tp.getFriends().remove(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "You removed " + ChatColor.GREEN + tp.getUsername() + ChatColor.RED +
                " from your Friend List!");
        tp.sendMessage(ChatColor.GREEN + player.getUsername() + ChatColor.RED + " removed you from their Friend List!");
        dashboard.getMongoHandler().removeFriend(player.getUniqueId(), tp.getUniqueId());
        dashboard.getMongoHandler().logActivity(player.getUniqueId(), "Remove Friend", name);
    }

    public static void acceptFriend(Player player, String name) {
        Dashboard dashboard = Launcher.getDashboard();
        HashMap<UUID, String> requestList = player.getRequests();
        UUID tuuid = null;
        for (Map.Entry<UUID, String> entry : requestList.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                tuuid = entry.getKey();
                break;
            }
        }
        if (tuuid == null) {
            player.sendMessage(ChatColor.RED + "That player hasn't sent you a friend request!");
            return;
        }
        player.getRequests().remove(tuuid);
        player.getFriends().put(tuuid, name);
        player.sendMessage(ChatColor.YELLOW + "You have accepted " + ChatColor.GREEN + name + "'s " + ChatColor.YELLOW +
                "Friend Request!");
        dashboard.getMongoHandler().acceptFriendRequest(player.getUniqueId(), tuuid);
        Player tp = dashboard.getPlayer(tuuid);
        if (tp != null) {
            tp.getFriends().put(player.getUniqueId(), player.getUsername());
            tp.sendMessage(player.getRank().getTagColor() + player.getUsername() + ChatColor.YELLOW +
                    " has accepted your Friend Request!");
        }
        dashboard.getMongoHandler().logActivity(player.getUniqueId(), "Accept Friend Request", name);
    }

    public static void denyFriend(Player player, String name) {
        Dashboard dashboard = Launcher.getDashboard();
        HashMap<UUID, String> requestList = player.getRequests();
        UUID tuuid = null;
        for (Map.Entry<UUID, String> entry : requestList.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                tuuid = entry.getKey();
                break;
            }
        }
        if (tuuid == null) {
            player.sendMessage(ChatColor.RED + "That player hasn't sent you a friend request!");
            return;
        }
        player.getRequests().remove(tuuid);
        player.sendMessage(ChatColor.RED + "You have denied " + ChatColor.GREEN + name + "'s " + ChatColor.RED +
                "Friend Request!");
        dashboard.getMongoHandler().denyFriendRequest(player.getUniqueId(), tuuid);
        dashboard.getMongoHandler().logActivity(player.getUniqueId(), "Denied Friend Request", name);
    }

    public static void helpMenu(Player player) {
        String dash = ChatColor.GREEN + "- " + ChatColor.AQUA;
        String y = ChatColor.YELLOW.toString();
        player.sendMessage(y + "Friend Commands:\n" + dash + "/friend help " + y + "- Shows this help menu\n" + dash +
                "/friend list [Page]" + y + "- Lists all of your friends\n" + dash + "/friend tp [player] " + y +
                "- Brings you to your friend's server\n" + dash + "/friend toggle " + y + "- Toggles friend requests\n"
                + dash + "/friend add [player] " + y + "- Asks a player to be your friend\n" + dash +
                "/friend remove [player] " + y + "- Removes a player as your friend\n" + dash +
                "/friend accept [player] " + y + "- Accepts someone's friend request\n" + dash +
                "/friend deny [player] " + y + "- Denies someone's friend request\n" + dash + "/friend requests " +
                y + "- Lists all of your friend requests");
    }

    public void friendMessage(Player player, HashMap<UUID, String> friendList, String joinMessage) {
        Dashboard dashboard = Launcher.getDashboard();
        if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
            for (Map.Entry<UUID, String> entry : friendList.entrySet()) {
                Player tp = dashboard.getPlayer(entry.getKey());
                if (tp != null) {
                    if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                        tp.sendMessage(joinMessage);
                    }
                }
            }
        } else {
            for (Map.Entry<UUID, String> entry : friendList.entrySet()) {
                Player tp = dashboard.getPlayer(entry.getKey());
                if (tp != null) {
                    tp.sendMessage(joinMessage);
                }
            }
        }
    }
}