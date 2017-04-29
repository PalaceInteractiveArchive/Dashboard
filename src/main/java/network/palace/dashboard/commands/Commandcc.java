package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class Commandcc extends MagicCommand {
    public String clearMessage = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";

    public Commandcc() {
        super(Rank.SQUIRE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        String server = player.getServer();
        boolean park = dashboard.getServer(server).isPark();
        for (Player tp : dashboard.getOnlinePlayers()) {
            if (tp.getServer().equals(server) || (park && dashboard.getServer(tp.getServer()).isPark())) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
                    tp.sendMessage(clearMessage + ChatColor.DARK_AQUA + "Chat has been cleared");
                } else {
                    tp.sendMessage("\n" + ChatColor.DARK_AQUA + "Chat has been cleared by " + player.getName());
                }
            }
        }
    }
}