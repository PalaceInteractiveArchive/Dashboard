package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class FindCommand extends DashboardCommand {

    public FindCommand() {
        super(Rank.TRAINEE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/find [Player]");
            return;
        }
        Player tp = Launcher.getDashboard().getPlayer(args[0]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + args[0] + " is not online!");
            return;
        }
        player.sendMessage(ChatColor.BLUE + tp.getUsername() + " is on the server " + ChatColor.GOLD + tp.getServer());
    }
}