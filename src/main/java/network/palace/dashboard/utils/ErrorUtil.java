package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Marc
 * @since 6/20/17
 */
public class ErrorUtil {
    private static List<UUID> debugIgnore = new ArrayList<>();

    public static void logError(String error, Throwable e) {
        Dashboard dashboard = Launcher.getDashboard();
        if (dashboard == null) {
            e.printStackTrace(System.out);
            return;
        }
        dashboard.getLogger().error(error + ": " + e.getMessage());
        dashboard.getErrors().error(error, e);
        String msg = ChatColor.RED + "[ERROR] " + ChatColor.WHITE + error;
        for (Player p : dashboard.getOnlinePlayers()) {
            if (p.getRank().getRankId() >= Rank.DEVELOPER.getRankId() && !debugIgnore.contains(p.getUniqueId())) {
                p.sendMessage(msg);
            }
        }
    }

    public static void logError(String error) {
        Dashboard dashboard = Launcher.getDashboard();
        if (dashboard == null) {
            return;
        }
        dashboard.getLogger().error(error);
        dashboard.getErrors().error(error);
        String msg = ChatColor.RED + "[ERROR] " + ChatColor.WHITE + error;
        for (Player p : dashboard.getOnlinePlayers()) {
            if (p.getRank().getRankId() >= Rank.DEVELOPER.getRankId() && !debugIgnore.contains(p.getUniqueId())) {
                p.sendMessage(msg);
            }
        }
    }

    public static boolean toggleDebugTracking(UUID uuid) {
        if (uuid == null)
            return false;
        if (debugIgnore.contains(uuid)) {
            debugIgnore.remove(uuid);
            return false;
        }
        debugIgnore.add(uuid);
        return true;
    }
}
