package network.palace.dashboard.commands.admin;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class ToggleServerQueueCommand extends DashboardCommand {

    public ToggleServerQueueCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (Launcher.getDashboard().getServerUtil().toggleServerQueueEnabled()) {
            player.sendMessage(ChatColor.GREEN + "Server queues have been enabled!");
        } else {
            player.sendMessage(ChatColor.RED + "Server queues have been disabled!");
        }
    }
}
