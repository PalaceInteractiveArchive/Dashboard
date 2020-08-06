package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketTitle;

import java.io.IOException;
import java.util.*;

/**
 * Created by Marc on 9/28/16
 */
public class AFKUtil {

    public AFKUtil() {
        Dashboard dashboard = Launcher.getDashboard();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Player tp : dashboard.getOnlinePlayers()) {
                    if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId() || tp.getRank().getRankId() >=
                            Rank.DEVELOPER.getRankId()) {
                        continue;
                    }
                    if (System.currentTimeMillis() - tp.getAfkTime() >= 1800000) {
                        if (tp.isAFK()) {
                            continue;
                        }
                        try {
                            warn(tp);
                        } catch (IOException e) {
                            Launcher.getDashboard().getLogger().error("Error processing AFK warn", e);
                        }
                    }
                }
            }
        }, 0, 5000);
    }

    public void warn(final Player player) throws IOException {
        Dashboard dashboard = Launcher.getDashboard();
        final UUID uuid = player.getUniqueId();
        String afk = ChatColor.RED + "" + ChatColor.BOLD + "                      AFK Timer:";
        String blank = "";
        String msg = ChatColor.YELLOW + "" + ChatColor.BOLD + "Type anything in chat (it won't be seen by others)";
        final List<String> msgs = Arrays.asList(blank, blank, afk, blank, msg, blank, blank, blank, blank, blank);
        final Timer t1 = new Timer();
        final Timer t2 = new Timer();
        player.setAFK(true);
        t2.schedule(new TimerTask() {
            @Override
            public void run() {
                t1.cancel();
                if (player != null && player.isAFK()) {
                    player.kickPlayer(ChatColor.RED + "You have been AFK for 30 minutes. Please try not to be AFK while on our servers.");
                    dashboard.getMongoHandler().logAFK(player.getUniqueId());
                } else {
                    cancel();
                }
            }
        }, 300000);
        t1.schedule(new TimerTask() {
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
    }
}