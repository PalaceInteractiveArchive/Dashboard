package network.palace.dashboard.commands.moderation;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class IPCommand extends DashboardCommand {

    public IPCommand() {
        super(Rank.LEAD);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/ip [Player]");
            return;
        }
        Player tp = Launcher.getDashboard().getPlayer(args[0]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + "That player wasn't found!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "IP of " + tp.getUsername() + " is " + tp.getAddress());
    }
}