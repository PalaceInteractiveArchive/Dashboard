package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.handlers.chat.ChatColor;
import network.palace.dashboard.utils.DateUtil;

import java.util.UUID;

public class MuteCommand extends DashboardCommand {

    public MuteCommand() {
        super(Rank.TRAINEE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
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
        dashboard.getSchedulerManager().runAsync(() -> {
            String reason;
            StringBuilder r = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                r.append(args[i]).append(" ");
            }
            reason = (r.substring(0, 1).toUpperCase() + r.substring(1)).trim();
            String source = player.getUniqueId().toString();
            Player tp = dashboard.getPlayer(username);
            UUID uuid;
            if (tp == null) {
                uuid = dashboard.getMongoHandler().usernameToUUID(username);
            } else {
                uuid = tp.getUniqueId();
                if (tp.getMute().isMuted()) {
                    player.sendMessage(ChatColor.RED + "This player is already muted! Unmute them to change the reason/duration.");
                    return;
                }
            }
            Mute mute = new Mute(uuid, username, true, System.currentTimeMillis(), muteTimestamp, reason, source);
            if (tp != null) {
                tp.setMute(mute);
            }
            dashboard.getMongoHandler().mutePlayer(uuid, mute);
            dashboard.getModerationUtil().announceMute(mute);
        });
    }
}