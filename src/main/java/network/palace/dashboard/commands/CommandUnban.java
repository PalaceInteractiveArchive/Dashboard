package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Rank;

import java.util.Arrays;
import java.util.UUID;

public class CommandUnban extends MagicCommand {

    public CommandUnban() {
        super(Rank.KNIGHT);
        aliases = Arrays.asList("pardon");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unban [Username]");
            return;
        }
        String username = args[0];
        UUID uuid = Dashboard.sqlUtil.uuidFromUsername(username);
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        Dashboard.sqlUtil.unbanPlayer(uuid);
        Dashboard.moderationUtil.announceUnban(username, player.getName());
    }
}