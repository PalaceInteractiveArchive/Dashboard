package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

/**
 * Created by Marc on 9/24/16
 */
public class Commandchatreload extends MagicCommand {

    public Commandchatreload() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        player.sendMessage(ChatColor.GREEN + "Reloading chat settings...");
        dashboard.getChatUtil().reload();
        dashboard.getSchedulerManager().getBroadcastClock().reload();
        player.sendMessage(ChatColor.GREEN + "Chat settings reloaded!");
    }
}