package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

public class LeaveServerQueueCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        String server;
        if ((server = Launcher.getDashboard().getServerUtil().leaveServerQueue(player)) != null) {
            player.sendMessage(ChatColor.GREEN + "You have left the queue to join " + ChatColor.YELLOW + server);
        }
    }
}
