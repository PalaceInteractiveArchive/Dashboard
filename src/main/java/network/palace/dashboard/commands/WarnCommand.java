package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.utils.WarningUtil;

public class WarnCommand extends DashboardCommand {

    public WarnCommand() {
        super(Rank.TRAINEE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/warn [Player] [Reason]");
            return;
        }
        Player tp = dashboard.getPlayer(args[0]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + "I can't find that player!");
            return;
        }
        if (System.currentTimeMillis() - tp.getWarningDelay() < 4000) {
            //players can't be warned until at least 4 seconds after their previous warn
            player.sendMessage(ChatColor.RED + "That player was warned recently, wait at least 4 seconds before warning again.");
            return;
        }
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        reason = reason.trim();
        tp.sendMessage(ChatColor.RED + "X===== " + ChatColor.YELLOW + "You have been issued a warning! " +
                ChatColor.RED + "=====X\n\n" + ChatColor.YELLOW + WarningUtil.getCenteredText(reason) + "\n\n" +
                ChatColor.RED + "X=======================================X");
        tp.setWarningDelay(System.currentTimeMillis());
        dashboard.getModerationUtil().announceWarning(tp.getUsername(), reason, player.getUsername());
        dashboard.getMongoHandler().warnPlayer(new Warning(tp.getUniqueId(), reason, player.getUniqueId().toString()));
    }
}
