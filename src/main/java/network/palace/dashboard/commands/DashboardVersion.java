package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Arrays;

public class DashboardVersion extends DashboardCommand {

    public DashboardVersion() {
        super(Rank.DEVELOPER);
        aliases.addAll(Arrays.asList("dashboardver", "dashver"));
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Dashboard currently running v" + Dashboard.getVersion());
    }
}
