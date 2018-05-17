package network.palace.dashboard.commands;

import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

/**
 * Created by Marc on 12/12/16.
 */
public class LinkCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.YELLOW + "You will be able to link your forum account with your Minecraft account soon!");
//        Launcher.getDashboard().getForum().linkAccount(player);
    }
}
