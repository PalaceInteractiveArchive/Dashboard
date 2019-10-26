package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Arrays;

public class UnbanIPCommand extends DashboardCommand {

    public UnbanIPCommand() {
        super(Rank.MOD);
        aliases = Arrays.asList("pardonip", "pardon-ip");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unbanip [IP Address]");
            return;
        }
        String address = args[0];
        dashboard.getMongoHandler().unbanAddress(address);
        dashboard.getModerationUtil().announceUnban("IP " + address, player.getUsername());
    }
}