package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class UnbanProviderCommand extends DashboardCommand {

    public UnbanProviderCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unbanprovider [Provider]");
            return;
        }
        StringBuilder provider = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            provider.append(args[i]);
            if (i < (args.length - 1)) {
                provider.append(" ");
            }
        }
        dashboard.getMongoHandler().unbanProvider(provider.toString());
        dashboard.getModerationUtil().announceUnban("Provider " + provider.toString(), player.getUsername());
    }
}