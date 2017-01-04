package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;

/**
 * Created by Marc on 12/12/16.
 */
public class CommandLink extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard.forum.linkAccount(player);
    }
}
