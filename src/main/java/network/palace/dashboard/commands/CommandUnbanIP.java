package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Rank;

import java.util.Arrays;

public class CommandUnbanIP extends MagicCommand {

    public CommandUnbanIP() {
        super(Rank.KNIGHT);
        aliases = Arrays.asList("pardonip", "pardon-ip");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unbanip [IP Address]");
            return;
        }
        String address = args[0];
        Dashboard.sqlUtil.unbanIP(address);
        Dashboard.moderationUtil.announceUnban("IP " + address, player.getName());
    }
}