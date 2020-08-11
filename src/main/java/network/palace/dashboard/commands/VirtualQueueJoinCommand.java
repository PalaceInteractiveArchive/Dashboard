package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

public class VirtualQueueJoinCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 1) Launcher.getDashboard().getParkQueueManager().joinQueue(player, args[0]);
    }
}
