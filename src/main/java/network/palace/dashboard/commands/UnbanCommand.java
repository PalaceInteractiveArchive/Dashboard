package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Collections;
import java.util.UUID;

public class UnbanCommand extends DashboardCommand {

    public UnbanCommand() {
        super(Rank.MOD);
        aliases = Collections.singletonList("pardon");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unban [Player] [Username]");
            return;
        }
        String username = args[0];
        UUID uuid = dashboard.getMongoHandler().usernameToUUID(username);
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        dashboard.getMongoHandler().unbanPlayer(uuid);
        dashboard.getModerationUtil().announceUnban(username, player.getUsername());
    }
}