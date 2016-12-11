package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;
import com.palacemc.dashboard.packets.dashboard.PacketBSeenCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CommandBSeen extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandBSeen() {
        super(Rank.SQUIRE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/bseen [Username]");
            return;
        }

        dashboard.getSchedulerManager().runAsync(() -> {
            Player tp = dashboard.getPlayer(args[0]);
            boolean online = tp != null;
            String name = online ? tp.getUsername() : args[0];
            UUID uuid;

            if (online) {
                uuid = tp.getUuid();
            } else {
                uuid = dashboard.getSqlUtil().uuidFromUsername(args[0]);
            }

            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "That player can't be found!");
                return;
            }

            Rank rank = Rank.SETTLER;
            long lastLogin = 0;
            String ip = "no ip";
            Mute mute;
            String server = "Unknown";

            if (online) {
                rank = tp.getRank();
                lastLogin = tp.getLoginTime();
                ip = tp.getAddress();
                mute = tp.getMute();
                server = tp.getServer();
            } else {
                try (Connection connection = dashboard.getSqlUtil().getConnection()) {
                    PreparedStatement sql = connection.prepareStatement("SELECT rank,lastseen,ipAddress,server FROM player_data WHERE uuid=?");

                    sql.setString(1, uuid.toString());

                    ResultSet result = sql.executeQuery();

                    if (result.next()) {
                        rank = Rank.fromString(result.getString("rank"));
                        lastLogin = result.getTimestamp("lastseen").getTime();
                        ip = result.getString("ipAddress");
                        server = result.getString("server");
                    }

                    result.close();
                    sql.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Ban ban = dashboard.getSqlUtil().getBan(uuid, name);

                if (ban != null) {
                    String type = ban.isPermanent() ? "Permanently" : ("Temporarily (Expires: " +
                            dashboard.getDateUtil().formatDateDiff(ban.getRelease()) + ")");
                    player.sendMessage(ChatColor.RED + name + " is Banned " + type + " for " + ban.getReason() +
                            " by " + ban.getSource());
                }

                mute = dashboard.getSqlUtil().getMute(uuid, name);
            }

            if (mute != null && mute.isMuted()) {
                player.sendMessage(ChatColor.RED + name + " is Muted for " +
                        dashboard.getDateUtil().formatDateDiff(mute.getRelease()) + " by " + mute.getSource() +
                        ". Reason: " + mute.getReason());
            }

            PacketBSeenCommand packet = new PacketBSeenCommand(player.getUuid(), name, ip, server, online);

            player.sendMessage(ChatColor.GREEN + name + " has been " + (online ? "online" : "away") + " for " +
                    dashboard.getDateUtil().formatDateDiff(lastLogin));
            player.sendMessage(ChatColor.RED + "Rank: " + rank.getNameWithBrackets());
            player.send(packet);
        });
    }
}