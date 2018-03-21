package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

import java.util.UUID;

public class BanCommand extends DashboardCommand {

    public BanCommand() {
        super(Rank.MOD);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player banner, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 2) {
            banner.sendMessage(ChatColor.RED + "/ban [Player] [Reason]");
            return;
        }
        String playername = args[0];
        UUID uuid = null;
        try {
            uuid = dashboard.getMongoHandler().usernameToUUID(playername);
        } catch (Exception ignored) {
        }
        if (uuid == null) {
            banner.sendMessage(ChatColor.RED + "I can't find that player!");
        }
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        String finalReason = reason.trim();
        UUID finalUuid = uuid;
        dashboard.getSchedulerManager().runAsync(() -> {
            try {
                if (dashboard.getMongoHandler().isPlayerBanned(finalUuid)) {
                    banner.sendMessage(ChatColor.RED + "This player is already banned! Unban them to change the reason.");
                    return;
                }
                Ban ban = new Ban(finalUuid, playername, true, System.currentTimeMillis(), System.currentTimeMillis(), finalReason, banner.getUniqueId().toString());
                dashboard.getMongoHandler().banPlayer(finalUuid, ban);
                Player tp = dashboard.getPlayer(finalUuid);
                if (tp != null) {
                    tp.kickPlayer(ChatColor.RED + "You Have Been Banned For " + ChatColor.AQUA + finalReason);
                }
                dashboard.getModerationUtil().announceBan(ban);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}