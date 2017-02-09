package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.utils.DateUtil;

import java.util.UUID;

public class Commandmute extends MagicCommand {

    public Commandmute() {
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
        Dashboard.schedulerManager.runAsync(() -> {
            String reason = "";
            String r = "";
            for (int i = 2; i < args.length; i++) {
                r += args[i] + " ";
            }
            reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
            String source = player.getName();
            Player tp = Dashboard.getPlayer(username);
            UUID uuid;
            if (tp == null) {
                uuid = Dashboard.sqlUtil.uuidFromUsername(username);
            } else {
                uuid = tp.getUniqueId();
            }
            Mute mute = new Mute(uuid, username, true, muteTimestamp, reason, source);
            if (tp != null) {
                tp.setMute(mute);
            }
            Dashboard.sqlUtil.mutePlayer(mute);
            Dashboard.moderationUtil.announceMute(mute);
        });
    }
}