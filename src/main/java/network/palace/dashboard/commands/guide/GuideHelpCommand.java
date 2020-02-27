package network.palace.dashboard.commands.guide;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.RankTag;

public class GuideHelpCommand extends DashboardCommand {

    public GuideHelpCommand() {
        super(Rank.TRAINEE, RankTag.GUIDE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.AQUA + "/h accept [username] - Accept a help request");
            player.sendMessage(ChatColor.AQUA + "/h tp [username] - Teleport cross-server to a player");
            return;
        }
        Dashboard dashboard = Launcher.getDashboard();
        Player tp = dashboard.getPlayer(args[1]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "accept": {
                dashboard.getGuideUtil().acceptHelpRequest(player, tp);
                break;
            }
            case "tp": {
                dashboard.getGuideUtil().teleport(player, tp);
                break;
            }
            default: {
                player.sendMessage(ChatColor.AQUA + "/h accept [username] - Accept a help request");
                player.sendMessage(ChatColor.AQUA + "/h tp [username] - Teleport cross-server to a player");
                break;
            }
        }
    }
}
