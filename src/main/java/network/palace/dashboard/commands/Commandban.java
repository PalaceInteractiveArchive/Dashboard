package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

import java.util.Date;
import java.util.UUID;

public class Commandban extends MagicCommand {

    public Commandban() {
        super(Rank.KNIGHT);
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
        UUID uuid;
        try {
            uuid = dashboard.getSqlUtil().uuidFromUsername(playername);
        } catch (Exception ignored) {
            banner.sendMessage(ChatColor.RED + "I can't find that player!");
            return;
        }
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        String finalReason = reason.trim();
        dashboard.getSchedulerManager().runAsync(() -> {
            if (dashboard.getSqlUtil().isBannedPlayer(uuid)) {
                banner.sendMessage(ChatColor.RED + "This player is already banned! Unban them to change the reason.");
                return;
            }
            dashboard.getSqlUtil().banPlayer(uuid, finalReason, true, new Date(System.currentTimeMillis()), banner.getName());
            Player tp = dashboard.getPlayer(uuid);
            if (tp != null) {
                tp.kickPlayer(ChatColor.RED + "You Have Been Banned For " + ChatColor.AQUA + finalReason);
            }
            dashboard.getModerationUtil().announceBan(new Ban(uuid, playername, true, System.currentTimeMillis(),
                    finalReason, banner.getName()));
        });
    }
}