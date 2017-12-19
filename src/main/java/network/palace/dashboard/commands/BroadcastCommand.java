package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class BroadcastCommand extends DashboardCommand {

    public BroadcastCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length > 0) {
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            String sname = player.getUsername();
            String msg = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" +
                    ChatColor.WHITE + "] " + ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message.toString());
            String staff = ChatColor.WHITE + "[" + ChatColor.AQUA +
                    sname + ChatColor.WHITE + "] " + ChatColor.GREEN +
                    ChatColor.translateAlternateColorCodes('&', message.toString());
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (dashboard.getPlayer(tp.getUniqueId()).getRank().getRankId() >= Rank.MOD.getRankId()) {
                    tp.sendMessage(staff);
                } else {
                    tp.sendMessage(msg);
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "/b [Message]");
        }
    }
}
