package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

/**
 * Created by Marc on 9/24/16
 */
public class CommandChatReload extends MagicCommand {

    public CommandChatReload() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Reloading chat settings...");
        Dashboard.chatUtil.reload();
        Dashboard.schedulerManager.getBroadcastClock().reload();
        player.sendMessage(ChatColor.GREEN + "Chat settings reloaded!");
    }
}