package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class Commandip extends MagicCommand {

    public Commandip() {
        super(Rank.SQUIRE);
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