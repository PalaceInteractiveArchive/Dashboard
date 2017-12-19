package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class ClearChatCommand extends DashboardCommand {
    public String clearMessage = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";

    public ClearChatCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        String server = player.getServer();
        boolean park = dashboard.getServer(server).isPark();
        for (Player tp : dashboard.getOnlinePlayers()) {
            if (tp.getServer().equals(server) || (park && dashboard.getServer(tp.getServer()).isPark())) {
                if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                    tp.sendMessage(clearMessage + ChatColor.DARK_AQUA + "Chat has been cleared");
                } else {
                    tp.sendMessage("\n" + ChatColor.DARK_AQUA + "Chat has been cleared by " + player.getUsername());
                }
            }
        }
    }
}