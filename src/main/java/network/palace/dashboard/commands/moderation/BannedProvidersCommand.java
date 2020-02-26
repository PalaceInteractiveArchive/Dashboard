package network.palace.dashboard.commands.moderation;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.List;

public class BannedProvidersCommand extends DashboardCommand {

    public BannedProvidersCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        List<String> bannedProviders = dashboard.getMongoHandler().getBannedProviders();
        if (bannedProviders.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "No Banned Providers!");
            return;
        }
        StringBuilder msg = new StringBuilder(ChatColor.GREEN + "Banned Providers:");
        for (String s : bannedProviders) {
            msg.append("\n- ").append(s);
        }
        player.sendMessage(msg.toString());
    }
}