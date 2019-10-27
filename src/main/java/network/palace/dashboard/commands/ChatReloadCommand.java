package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

/**
 * Created by Marc on 9/24/16
 */
public class ChatReloadCommand extends DashboardCommand {

    public ChatReloadCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        player.sendMessage(ChatColor.GREEN + "Reloading chat settings (swears and links)...");
        dashboard.getChatUtil().reload();
        dashboard.getSchedulerManager().getBroadcastClock().reload();
        player.sendMessage(ChatColor.GREEN + "Chat settings reloaded!");
    }
}