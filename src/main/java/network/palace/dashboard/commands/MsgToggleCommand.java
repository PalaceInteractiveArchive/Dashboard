package network.palace.dashboard.commands;

import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class MsgToggleCommand extends DashboardCommand {

    public MsgToggleCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.setReceiveMessages(!player.canRecieveMessages());
        String modifier = player.canRecieveMessages() ? ChatColor.GREEN + "enabled " : ChatColor.RED + "disabled ";
        player.sendMessage(ChatColor.YELLOW + "You have " + modifier + ChatColor.YELLOW +
                "receiving private messages!");
    }
}