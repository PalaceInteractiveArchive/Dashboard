package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

public class KickCommand extends DashboardCommand {

    public KickCommand() {
        super(Rank.TRAINEE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/kick [Player] [Reason]");
            return;
        }
        Player tp = dashboard.getPlayer(args[0]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + "I can't find that player!");
            return;
        }
        if (tp.isKicking()) {
            player.sendMessage(ChatColor.RED + "I can't find that player!");
            return;
        }
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        reason = reason.trim();
        tp.kickPlayer(ChatColor.RED + "You have been disconnected for: " + ChatColor.AQUA + reason);
        try {
            dashboard.getModerationUtil().announceKick(tp.getUsername(), reason, player.getUsername());
            dashboard.getMongoHandler().logKick(new Kick(tp.getUniqueId(), reason, player.getUsername()));
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "That player isn't online!");
        }
    }
}