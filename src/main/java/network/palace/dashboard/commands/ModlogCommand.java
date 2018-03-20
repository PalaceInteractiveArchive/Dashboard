package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.UUID;

public class ModlogCommand extends DashboardCommand {

    public ModlogCommand() {
        super(Rank.TRAINEE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/modlog [Username] [Bans/Mutes/Kicks]");
            return;
        }
        String username = args[0];
        Player tp = dashboard.getPlayer(username);
        UUID uuid;
        if (tp == null) {
            uuid = dashboard.getMongoHandler().usernameToUUID(username);
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }
        } else {
            uuid = tp.getUniqueId();
            username = tp.getUsername();
        }
        if (args.length == 1) {
            int bans = 0;
            int mutes = 0;
            int kicks = 0;
            player.sendMessage(ChatColor.GOLD + "Mod Log for " + username + ":");
        } else {
        }
    }

    /*@Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        try {
            if (args.length < 1 || args.length > 2) {
                player.sendMessage(ChatColor.RED + "/modlog [Username] [Bans/Mutes/Kicks]");
                return;
            }
            String playername = args[0];
            Player tp = dashboard.getPlayer(playername);
            UUID uuid;
            if (tp == null) {
                try {
                    uuid = dashboard.getMongoHandler().usernameToUUID(playername);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
            } else {
                uuid = tp.getUniqueId();
            }
            String action;
            Optional<Connection> optConnection = dashboard.getMongoHandler().getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            if (args.length > 1) {
                action = args[1].toLowerCase();
                switch (action) {
                    case "bans": {
                        List<String> msgs = new ArrayList<>();
                        try (Connection connection = optConnection.get()) {
                            PreparedStatement sql = connection.prepareStatement("SELECT reason,permanent,`release`,source,active FROM banned_players WHERE uuid=?");
                            sql.setString(1, uuid.toString());
                            ResultSet result = sql.executeQuery();
                            while (result.next()) {
                                String msg = ChatColor.RED + "Reason: " + ChatColor.GREEN + result.getString("reason") +
                                        ChatColor.RED + " | " + ChatColor.GREEN + (result.getInt("permanent") == 1 ?
                                        "Permanent" : "Temporary") + ChatColor.RED + " | ";
                                if (result.getInt("permanent") != 1) {
                                    msg += "Expires: " + ChatColor.GREEN +
                                            DateUtil.formatDateDiff(result.getTimestamp("release").getTime()) +
                                            ChatColor.RED + " | ";
                                }
                                msg += "Source: " + ChatColor.GREEN + result.getString("source") + ChatColor.RED +
                                        " | Active: " + ChatColor.GREEN + (result.getInt("active") == 1 ? "True" : "False");
                                msgs.add(msg);
                            }
                            result.close();
                            sql.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Ban Log for " + playername + ":");
                        if (msgs.isEmpty()) {
                            player.sendMessage(ChatColor.GREEN + "No bans!");
                            return;
                        }
                        StringBuilder message = new StringBuilder();
                        for (int i = 0; i < msgs.size(); i++) {
                            String msg = msgs.get(i);
                            message.append(msg);
                            if (i < (msgs.size() - 1)) {
                                message.append("\n");
                            }
                        }
                        player.sendMessage(message.toString());
                        return;
                    }
                    case "mutes": {
                        List<String> msgs = new ArrayList<>();
                        try (Connection connection = optConnection.get()) {
                            PreparedStatement sql = connection.prepareStatement("SELECT reason,`release`,source,active FROM muted_players WHERE uuid=?");
                            sql.setString(1, uuid.toString());
                            ResultSet result = sql.executeQuery();
                            while (result.next()) {
                                boolean active = result.getInt("active") == 1;
                                String msg = ChatColor.RED + "Reason: " + ChatColor.GREEN + result.getString("reason").trim() +
                                        ChatColor.RED + " | Source: " + ChatColor.GREEN + result.getString("source");
                                if (active) {
                                    msg += ChatColor.RED + " | Expires: " + ChatColor.GREEN +
                                            DateUtil.formatDateDiff(result.getTimestamp("release").getTime());
                                }
                                msg += ChatColor.RED + " | Active: " + ChatColor.GREEN + (active ? "True" : "False");
                                msgs.add(msg);
                            }
                            result.close();
                            sql.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Mute Log for " + playername + ":");
                        if (msgs.isEmpty()) {
                            player.sendMessage(ChatColor.GREEN + "No mutes!");
                            return;
                        }
                        StringBuilder message = new StringBuilder();
                        for (int i = 0; i < msgs.size(); i++) {
                            String msg = msgs.get(i);
                            message.append(msg);
                            if (i < (msgs.size() - 1)) {
                                message.append("\n");
                            }
                        }
                        player.sendMessage(message.toString());
                        return;
                    }
                    case "kicks": {
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        List<String> msgs = new ArrayList<>();
                        try (Connection connection = optConnection.get()) {
                            PreparedStatement sql = connection.prepareStatement("SELECT reason,source,time FROM kicks WHERE uuid=?");
                            sql.setString(1, uuid.toString());
                            ResultSet result = sql.executeQuery();
                            while (result.next()) {
                                String msg = ChatColor.RED + "Reason: " + ChatColor.GREEN + result.getString("reason").trim() +
                                        ChatColor.RED + " | Source: " + ChatColor.GREEN + result.getString("source") +
                                        ChatColor.RED + " | Time: " + ChatColor.GREEN + df.format(result.getTimestamp("time"));
                                msgs.add(msg);
                            }
                            result.close();
                            sql.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Kick Log for " + playername + ":");
                        if (msgs.isEmpty()) {
                            player.sendMessage(ChatColor.GREEN + "No kicks!");
                            return;
                        }
                        StringBuilder message = new StringBuilder();
                        for (int i = 0; i < msgs.size(); i++) {
                            String msg = msgs.get(i);
                            message.append(msg);
                            if (i < (msgs.size() - 1)) {
                                message.append("\n");
                            }
                        }
                        player.sendMessage(message.toString());
                        return;
                    }
                }
            }
            if (args.length == 1) {
                int banCount = 0;
                int muteCount = 0;
                int kickCount = 0;
                try (Connection connection = optConnection.get()) {
                    PreparedStatement bans = connection.prepareStatement("SELECT count(*) FROM banned_players WHERE uuid=?");
                    bans.setString(1, uuid.toString());
                    ResultSet bansresult = bans.executeQuery();
                    bansresult.next();
                    banCount = bansresult.getInt("count(*)");
                    bansresult.close();
                    bans.close();
                    PreparedStatement mutes = connection.prepareStatement("SELECT count(*) FROM muted_players WHERE uuid=?");
                    mutes.setString(1, uuid.toString());
                    ResultSet mutesresult = mutes.executeQuery();
                    mutesresult.next();
                    muteCount = mutesresult.getInt("count(*)");
                    mutesresult.close();
                    mutes.close();
                    PreparedStatement kicks = connection.prepareStatement("SELECT count(*) FROM kicks WHERE uuid=?");
                    kicks.setString(1, uuid.toString());
                    ResultSet kicksresult = kicks.executeQuery();
                    kicksresult.next();
                    kickCount = kicksresult.getInt("count(*)");
                    kicksresult.close();
                    kicks.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                player.sendMessage(ChatColor.GREEN + "Moderation Log for " + playername + ": " + ChatColor.YELLOW +
                        banCount + " Bans, " + muteCount + " Mutes, " + kickCount + " Kicks");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}