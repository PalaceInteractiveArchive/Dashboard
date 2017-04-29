package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.utils.NameUtil;

import java.util.List;

public class Commandnamecheck extends MagicCommand {

    public Commandnamecheck() {
        super(Rank.SQUIRE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/namecheck [username]");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Requesting name history for " + ChatColor.AQUA + args[0] +
                ChatColor.GREEN + " from Mojang...");
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            List<String> names = NameUtil.getNames(args[0]);
            if (names.size() < 2 || names.isEmpty()) {
                player.sendMessage(ChatColor.RED + "That user could not be found!");
                return;
            }
            String msg = ChatColor.GREEN + "Previous usernames of " + args[0] + " are:";
            for (int i = 1; i < names.size(); i++) {
                msg += ChatColor.AQUA + "\n- " + ChatColor.GREEN + names.get(i);
            }
            player.sendMessage(msg);
        });
    }
}