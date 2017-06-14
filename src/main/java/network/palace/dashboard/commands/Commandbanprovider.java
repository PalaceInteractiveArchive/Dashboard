package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class Commandbanprovider extends MagicCommand {

    public Commandbanprovider() {
        super(Rank.KNIGHT);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player banner, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            banner.sendMessage(ChatColor.RED + "/banprovider [Provider]");
            return;
        }
        StringBuilder provider = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            provider.append(args[i]);
            if (i < (args.length - 1)) {
                provider.append(" ");
            }
        }
    }
}