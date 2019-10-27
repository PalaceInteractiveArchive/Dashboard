package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

public class OnlineCountCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "\nTotal Players Online: " + Launcher.getDashboard().getOnlinePlayers().size() + "\n");
    }
}