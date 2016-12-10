package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;
import com.palacemc.dashboard.utils.DateUtil;

import java.util.UUID;

public class CommandMute extends MagicCommand {

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
        final long muteTimestamp = DateUtil.parseDateDiff(args[1], true);
        long length = muteTimestamp - System.currentTimeMillis();

        if (length > 3600000) {
            player.sendMessage(ChatColor.RED + "The maximum mute length is 1 hour!");
            return;
        }

        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            String reason;
            String r = "";

            for (int i = 2; i < args.length; i++) {
                r += args[i] + " ";
            }

            reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
            String source = player.getName();
            Player tp = Launcher.getDashboard().getPlayer(username);
            UUID uuid;

            if (tp == null) {
                uuid = Launcher.getDashboard().getSqlUtil().uuidFromUsername(username);
            } else {
                uuid = tp.getUniqueId();
            }

            Mute mute = new Mute(uuid, username, true, muteTimestamp, reason, source);

            if (tp != null) {
                tp.setMute(mute);
            }

            Launcher.getDashboard().getSqlUtil().mutePlayer(mute);
            Launcher.getDashboard().getModerationUtil().announceMute(mute);
        });
    }
}