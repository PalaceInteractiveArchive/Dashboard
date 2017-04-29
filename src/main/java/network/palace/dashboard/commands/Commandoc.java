package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;

public class Commandoc extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "\nTotal Players Online: " + Launcher.getDashboard().getOnlinePlayers().size() + "\n");
    }
}