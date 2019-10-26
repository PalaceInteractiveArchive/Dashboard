package network.palace.dashboard.commands;

import network.palace.dashboard.handlers.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

public class WhereAmICommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.BLUE + "You are on the server " + ChatColor.GOLD + player.getServer());
    }
}