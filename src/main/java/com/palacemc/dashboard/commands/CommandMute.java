package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;

import java.util.UUID;

public class CommandMute extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandMute() {
        super(Rank.SQUIRE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "/mute [Player] [Time] [Reason]");
            player.sendMessage(ChatColor.RED + "Time Examples:");
            player.sendMessage(ChatColor.RED + "5m = Five Minutes");
            player.sendMessage(ChatColor.RED + "1h = One Hour");
            return;
        }

        final String username = args[0];
        final long muteTimestamp = dashboard.getDateUtil().parseDateDiff(args[1], true);
        long length = muteTimestamp - System.currentTimeMillis();

        if (length > 3600000) {
            player.sendMessage(ChatColor.RED + "The maximum mute length is 1 hour!");
            return;
        }

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

            Mute mute = new Mute(uuid, username, reason, source, true, muteTimestamp);

            if (tp != null) {
                tp.setMute(mute);
            }

            dashboard.getSqlUtil().mutePlayer(mute);
            dashboard.getModerationUtil().announceMute(mute);
        });
    }
}