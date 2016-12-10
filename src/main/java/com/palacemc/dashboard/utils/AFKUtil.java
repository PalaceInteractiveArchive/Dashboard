package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.packets.dashboard.PacketTitle;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Marc on 9/28/16
 */
public class AFKUtil {

    public AFKUtil() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                    if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId() || tp.getRank().getRankId() >=
                            Rank.WIZARD.getRankId()) {
                        continue;
                    }
                    if (System.currentTimeMillis() - tp.getAfkTime() >= 1800000) {
                        if (tp.isAFK()) {
                            continue;
                        }
                        try {
                            warn(tp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 0, 5000);
    }

    public void warn(final Player player) throws IOException {
        final UUID uuid = player.getUniqueId();
        String afk = ChatColor.RED + "" + ChatColor.BOLD + "                      AFK Timer:";
        String blank = "";
        String msg = ChatColor.YELLOW + "" + ChatColor.BOLD + "Type anything in chat (it won't be seen by others)";
        final List<String> msgs = Arrays.asList(blank, blank, afk, blank, msg, blank, blank, blank, blank, blank);
        final Timer id = new Timer();
        player.setAFK(true);
        id.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                try {
                    if (player != null && player.isAFK()) {
                        PacketTitle packet = new PacketTitle(player.getUniqueId(), ChatColor.RED + "" + ChatColor.BOLD +
                                "Are you AFK?", ChatColor.RED + "AFK kick in " + ChatColor.DARK_RED + (5 - i) + " " +
                                ChatColor.RED + "minutes!", 10, 1200, 20);
                        player.send(packet);
                        i++;
                        for (String m : msgs) {
                            player.sendMessage(m);
                        }
                    } else {
                        cancel();
                    }
                } catch (Exception ignored) {
                }
            }
        }, 0, 60000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                id.cancel();
                try {
                    if (player != null && player.isAFK()) {
                        player.kickPlayer(ChatColor.RED + "You have been AFK for 30 minutes. Please try not to be AFK while on our servers.");
                        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
                            PreparedStatement sql = connection.prepareStatement("INSERT INTO afklogs (`user`) VALUES('" +
                                    uuid + "')");
                            sql.execute();
                            sql.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        cancel();
                    }
                } catch (Exception ignored) {
                }
            }
        }, 300000);
    }
}