package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.packets.dashboard.PacketFriendRequest;
import com.palacemc.dashboard.packets.dashboard.PacketListFriendCommand;
import com.palacemc.dashboard.packets.dashboard.PacketListRequestCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Marc on 8/22/16
 */
public class FriendUtil {

    public static void teleportPlayer(Player player, Player target) {
        if (target == null) {
            return;
        }
        if (player.getServer().equals(target.getServer())) {
            player.sendMessage(ChatColor.RED + "You're already on the same server as " + ChatColor.AQUA +
                    target.getName() + "!");
            return;
        }
        try {
            Launcher.getDashboard().getServerUtil().sendPlayer(player, target.getServer());
            player.sendMessage(ChatColor.BLUE + "You connected to the server " + ChatColor.GREEN + target.getName() +
                    " " + ChatColor.BLUE + "is on! (" + target.getServer() + ")");
        } catch (Exception ignored) {
        }
    }

    public static void listFriends(final Player player, int page) {
        HashMap<UUID, String> friends = player.getFriends();
        if (friends.isEmpty()) {
            player.sendMessage(" ");
            player.sendMessage(ChatColor.RED + "Type /friend add [Player] to add someone");
            player.sendMessage(" ");
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
            Player tp = Launcher.getDashboard().getPlayer(entry.getKey());
            if (tp == null) {
                currentFriends.add(entry.getValue());
            } else {
                currentFriends.add(entry.getValue() + ":" + tp.getServer());
            }
        }
        Collections.sort(currentFriends);
        List<String> fsOnPage = new ArrayList<>();
        for (String s : currentFriends.subList(startAmount, endAmount)) {
            fsOnPage.add(s);
        }
        PacketListFriendCommand packet = new PacketListFriendCommand(player.getUniqueId(), page, maxPage, fsOnPage);
        player.send(packet);
    }

    public static void toggleRequests(Player player) {
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET toggled=? WHERE uuid=?");
            sql.setInt(1, player.hasFriendToggledOff() ? 1 : 0);
            sql.setString(2, player.getUniqueId().toString());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void listRequests(final Player player) {
        HashMap<UUID, String> requests = player.getRequests();
        if (requests.isEmpty()) {
            player.sendMessage(" ");
            player.sendMessage(ChatColor.RED + "You currently have no Friend Requests!");
            player.sendMessage(" ");
            return;
        }
        PacketListRequestCommand packet = new PacketListRequestCommand(player.getUniqueId(), new ArrayList<>(requests.values()));
        player.send(packet);
    }

    private static HashMap<UUID, String> getList(UUID uuid, int status) {
        List<UUID> uuids = new ArrayList<>();
        HashMap<UUID, String> map = new HashMap<>();
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            switch (status) {
                case 0: {
                    PreparedStatement sql = connection.prepareStatement("SELECT sender FROM friends WHERE receiver=? AND status=0");
                    sql.setString(1, uuid.toString());
                    ResultSet result = sql.executeQuery();
                    while (result.next()) {
                        uuids.add(UUID.fromString(result.getString("sender")));
                    }
                    result.close();
                    sql.close();
                    break;
                }
                case 1: {
                    PreparedStatement sql = connection.prepareStatement("SELECT sender,receiver FROM friends WHERE (sender=? OR receiver=?) AND status=1");
                    sql.setString(1, uuid.toString());
                    sql.setString(2, uuid.toString());
                    ResultSet result = sql.executeQuery();
                    while (result.next()) {
                        if (result.getString("sender").equalsIgnoreCase(uuid.toString())) {
                            uuids.add(UUID.fromString(result.getString("receiver")));
                        } else {
                            uuids.add(UUID.fromString(result.getString("sender")));
                        }
                    }
                    break;
                }
            }
            if (uuids.isEmpty()) {
                return map;
            }
            String query = "SELECT username,uuid FROM player_data WHERE uuid=";
            for (int i = 0; i < uuids.size(); i++) {
                if (i >= (uuids.size() - 1)) {
                    query += "?";
                } else {
                    query += "? or uuid=";
                }
            }
            PreparedStatement sql2 = connection.prepareStatement(query);
            for (int i = 1; i < (uuids.size() + 1); i++) {
                sql2.setString(i, uuids.get(i - 1).toString());
            }
            ResultSet res2 = sql2.executeQuery();
            while (res2.next()) {
                map.put(UUID.fromString(res2.getString("uuid")), res2.getString("username"));
            }
            res2.close();
            sql2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static HashMap<UUID, String> getFriendList(UUID uuid) {
        return getList(uuid, 1);
    }

    public static HashMap<UUID, String> getRequestList(UUID uuid) {
        return getList(uuid, 0);
    }

    public static void addFriend(Player player, String name) {
        if (name.equalsIgnoreCase(player.getName())) {
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
        Player tp = Launcher.getDashboard().getPlayer(name);
        if (tp == null) {
            try {
                UUID tuuid = Launcher.getDashboard().getSqlUtil().uuidFromUsername(name);
                HashMap<UUID, String> requests = getList(tuuid, 0);
                if (requests.containsKey(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You have already sent this player a Friend Request!");
                    return;
                }
                if (player.getRank().getRankId() < Rank.KNIGHT.getRankId()) {
                    if (hasFriendsToggledOff(tuuid)) {
                        player.sendMessage(ChatColor.RED + "That player has Friend Requests toggled off!");
                        return;
                    }
                }
                player.sendMessage(ChatColor.YELLOW + "You have sent " + ChatColor.AQUA + name + ChatColor.YELLOW +
                        " a Friend Request!");
                /**
                 * Add request to database
                 */
                try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
                    PreparedStatement sql = connection.prepareStatement("INSERT INTO friends (sender,receiver) VALUES (?,?)");
                    sql.setString(1, player.getUniqueId().toString());
                    sql.setString(2, tuuid.toString());
                    sql.execute();
                    sql.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Launcher.getDashboard().getActivityUtil().logActivity(player.getUniqueId(), "Send Friend Request", name);
            } catch (Exception ignored) {
                player.sendMessage(ChatColor.RED + "That player could not be found!");
            }
            return;
        }
        if (tp.getRequests().containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have already sent this player a Friend Request!");
            return;
        }
        if (player.getRank().getRankId() < Rank.KNIGHT.getRankId()) {
            if (tp.hasFriendToggledOff()) {
                player.sendMessage(ChatColor.RED + "That player has Friend Requests toggled off!");
                return;
            }
        }
        tp.getRequests().put(player.getUniqueId(), player.getName());
        player.sendMessage(ChatColor.YELLOW + "You have sent " + ChatColor.AQUA + tp.getName() + ChatColor.YELLOW +
                " a Friend Request!");
        PacketFriendRequest packet = new PacketFriendRequest(tp.getUniqueId(), player.getName());
        tp.send(packet);
        /**
         * Add request to database
         */
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO friends (sender,receiver) VALUES (?,?)");
            sql.setString(1, player.getUniqueId().toString());
            sql.setString(2, tp.getUniqueId().toString());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Launcher.getDashboard().getActivityUtil().logActivity(player.getUniqueId(), "Send Friend Request", name);
    }

    public static void removeFriend(Player player, String name) {
        Player tp = Launcher.getDashboard().getPlayer(name);
        if (tp == null) {
            try {
                UUID tuuid = Launcher.getDashboard().getSqlUtil().uuidFromUsername(name);
                if (!player.getFriends().containsKey(tuuid)) {
                    player.sendMessage(ChatColor.RED + "That player isn't on your Friend List!");
                    return;
                }
                player.getFriends().remove(tuuid);
                player.sendMessage(ChatColor.RED + "You removed " + ChatColor.AQUA + name + ChatColor.RED +
                        " from your Friend List!");
                try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
                    PreparedStatement sql = connection.prepareStatement("DELETE FROM friends WHERE (sender=? OR receiver=?) AND (sender=? OR receiver=?)");
                    sql.setString(1, player.getUniqueId().toString());
                    sql.setString(2, player.getUniqueId().toString());
                    sql.setString(3, tuuid.toString());
                    sql.setString(4, tuuid.toString());
                    sql.execute();
                    sql.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (Exception ignored) {
                player.sendMessage(ChatColor.RED + "That player could not be found!");
            }
            Launcher.getDashboard().getActivityUtil().logActivity(player.getUniqueId(), "Remove Friend", name);
            return;
        }
        if (!player.getFriends().containsKey(tp.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "That player isn't on your Friend List!");
            return;
        }
        player.getFriends().remove(tp.getUniqueId());
        tp.getFriends().remove(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "You removed " + ChatColor.GREEN + tp.getName() + ChatColor.RED +
                " from your Friend List!");
        tp.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.RED + " removed you from their Friend List!");
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("DELETE FROM friends WHERE (sender=? OR receiver=?) AND (sender=? OR receiver=?)");
            sql.setString(1, player.getUniqueId().toString());
            sql.setString(2, player.getUniqueId().toString());
            sql.setString(3, tp.getUniqueId().toString());
            sql.setString(4, tp.getUniqueId().toString());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Launcher.getDashboard().getActivityUtil().logActivity(player.getUniqueId(), "Remove Friend", name);
    }

    public static void acceptFriend(Player player, String name) {
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
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("UPDATE friends SET status=1 WHERE (sender=? OR receiver=?) AND (sender=? OR receiver=?)");
            sql.setString(1, player.getUniqueId().toString());
            sql.setString(2, player.getUniqueId().toString());
            sql.setString(3, tuuid.toString());
            sql.setString(4, tuuid.toString());
            sql.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Player tp = Launcher.getDashboard().getPlayer(tuuid);
        if (tp != null) {
            tp.getFriends().put(player.getUniqueId(), player.getName());
            tp.sendMessage(player.getRank().getTagColor() + player.getName() + ChatColor.YELLOW +
                    " has accepted your Friend Request!");
        }
        Launcher.getDashboard().getActivityUtil().logActivity(player.getUniqueId(), "Accept Friend Request", name);
    }

    public static void denyFriend(Player player, String name) {
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
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("DELETE FROM friends WHERE (sender=? OR receiver=?) AND (sender=? OR receiver=?)");
            sql.setString(1, player.getUniqueId().toString());
            sql.setString(2, player.getUniqueId().toString());
            sql.setString(3, tuuid.toString());
            sql.setString(4, tuuid.toString());
            sql.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Launcher.getDashboard().getActivityUtil().logActivity(player.getUniqueId(), "Denied Friend Request", name);
    }

    public static boolean hasFriendsToggledOff(UUID uuid) {
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM player_data WHERE uuid=?");
            sql.setString(1, uuid.toString());
            ResultSet result = sql.executeQuery();
            result.next();
            boolean toggledOff = result.getInt("toggled") == 1;
            result.close();
            sql.close();
            return toggledOff;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
}