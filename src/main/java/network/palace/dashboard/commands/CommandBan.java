package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;

import java.util.Date;
import java.util.UUID;

public class CommandBan extends MagicCommand {

    public CommandBan() {
        super(Rank.KNIGHT);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player banner, String label, String[] args) {
        if (args.length < 2) {
            banner.sendMessage(ChatColor.RED + "/ban [Player] [Reason]");
            return;
        }
        String playername = args[0];
        UUID uuid;
        try {
            uuid = Dashboard.sqlUtil.uuidFromUsername(playername);
        } catch (Exception ignored) {
            banner.sendMessage(ChatColor.RED + "I can't find that player!");
            return;
        }
        String r = "";
        for (int i = 1; i < args.length; i++) {
            r += args[i] + " ";
        }
        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        reason = reason.trim();
        if (Dashboard.sqlUtil.isBannedPlayer(uuid)) {
            banner.sendMessage(ChatColor.RED + "This player is already banned! Unban them to change the reason.");
            return;
        }
        Dashboard.sqlUtil.banPlayer(uuid, reason, true, new Date(System.currentTimeMillis()), banner.getName());
        Player tp = Dashboard.getPlayer(uuid);
        if (tp != null) {
            tp.kickPlayer(ChatColor.RED + "You Have Been Banned For " + ChatColor.AQUA + reason);
        }
        Dashboard.moderationUtil.announceBan(new Ban(uuid, playername, true, System.currentTimeMillis(), reason, banner.getName()));
    }
}