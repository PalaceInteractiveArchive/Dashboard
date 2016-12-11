package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;

import java.sql.Date;
import java.util.UUID;

public class CommandTempBan extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandTempBan() {
        super(Rank.KNIGHT);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "/tempban [Player] [Time] [Reason]");
            player.sendMessage(ChatColor.RED + "Time Examples:");
            player.sendMessage(ChatColor.RED + "6h = Six Hours");
            player.sendMessage(ChatColor.RED + "6d = Six Days");
            player.sendMessage(ChatColor.RED + "6w = Six Weeks");
            player.sendMessage(ChatColor.RED + "6mon = Six Months");
            return;
        }

        final String username = args[0];
        final long timestamp = dashboard.getDateUtil().parseDateDiff(args[1], true);

        dashboard.getSchedulerManager().runAsync(() -> {
            String reason;
            String r = "";

            for (int i = 2; i < args.length; i++) {
                r += args[i] + " ";
            }

            reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
            String source = player.getUsername();
            Player tp = dashboard.getPlayer(username);
            UUID uuid;

            if (tp == null) {
                uuid = dashboard.getSqlUtil().uuidFromUsername(username);
            } else {
                uuid = tp.getUuid();
            }

            Ban ban = new Ban(uuid, username, false, timestamp, reason, source);

            if (tp != null) {
                tp.kickPlayer(ChatColor.RED + "You Have Been Temporarily Banned For " + ChatColor.AQUA + reason +
                        ". " + ChatColor.RED + "Your Temporary Ban Will Expire in " + ChatColor.AQUA +
                        dashboard.getDateUtil().formatDateDiff(timestamp));
            }

            dashboard.getSqlUtil().banPlayer(uuid, reason, false, new Date(timestamp), source);
            dashboard.getModerationUtil().announceBan(ban);
        });
    }
}