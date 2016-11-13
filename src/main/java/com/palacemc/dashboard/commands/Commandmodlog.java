package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.utils.DateUtil;
import com.palacemc.dashboard.handlers.Rank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Commandmodlog extends MagicCommand {

    public Commandmodlog() {
        super(Rank.SQUIRE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        try {
            if (args.length < 1 || args.length > 2) {
                player.sendMessage(ChatColor.RED + "/modlog [Username] [Bans/Mutes/Kicks]");
                return;
            }
            String playername = args[0];
            Player tp = Dashboard.getPlayer(playername);
            UUID uuid = null;
            if (tp == null) {
                try {
                    uuid = Dashboard.sqlUtil.uuidFromUsername(playername);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
            } else {
                uuid = tp.getUniqueId();
            }
            String action = "";
            if (args.length > 1) {
                action = args[1].toLowerCase();
                switch (action) {
                    case "bans": {
                        List<String> msgs = new ArrayList<>();
                        try (Connection connection = Dashboard.sqlUtil.getConnection()) {
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
                        String message = "";
                        for (int i = 0; i < msgs.size(); i++) {
                            String msg = msgs.get(i);
                            message += msg;
                            if (i < (msgs.size() - 1)) {
                                message += "\n";
                            }
                        }
                        player.sendMessage(message);
                        return;
                    }
                    case "mutes": {
                        List<String> msgs = new ArrayList<>();
                        try (Connection connection = Dashboard.sqlUtil.getConnection()) {
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
                        String message = "";
                        for (int i = 0; i < msgs.size(); i++) {
                            String msg = msgs.get(i);
                            message += msg;
                            if (i < (msgs.size() - 1)) {
                                message += "\n";
                            }
                        }
                        player.sendMessage(message);
                        return;
                    }
                    case "kicks": {
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        List<String> msgs = new ArrayList<>();
                        try (Connection connection = Dashboard.sqlUtil.getConnection()) {
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
                        String message = "";
                        for (int i = 0; i < msgs.size(); i++) {
                            String msg = msgs.get(i);
                            message += msg;
                            if (i < (msgs.size() - 1)) {
                                message += "\n";
                            }
                        }
                        player.sendMessage(message);
                        return;
                    }
                }
            }
            if (args.length == 1) {
                int banCount = 0;
                int muteCount = 0;
                int kickCount = 0;
                try (Connection connection = Dashboard.sqlUtil.getConnection()) {
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
    }
}