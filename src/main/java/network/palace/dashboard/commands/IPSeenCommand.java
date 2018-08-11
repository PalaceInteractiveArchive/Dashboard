package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketIPSeenCommand;

import java.util.List;

public class IPSeenCommand extends DashboardCommand {

    public IPSeenCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/ipseen [IP Address]");
            return;
        }
        dashboard.getSchedulerManager().runAsync(() -> {
            AddressBan ban = dashboard.getMongoHandler().getAddressBan(args[0]);
            if (ban != null) {
                player.sendMessage(ChatColor.RED + "This IP Address is banned for " + ChatColor.AQUA + ban.getReason());
            }
            SpamIPWhitelist spamIPWhitelist = dashboard.getMongoHandler().getSpamIPWhitelist(args[0]);
            if (spamIPWhitelist != null) {
                player.sendMessage(ChatColor.GREEN + "This IP Address is whitelisted from Spam IP protection with a player limit of " +
                        spamIPWhitelist.getLimit() + " players.");
            }
            List<String> users = dashboard.getMongoHandler().getPlayersOnIP(args[0]);
            if (users == null || users.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No users found on that IP Address.");
                return;
            }
            if (users.size() > 30) {
                player.sendMessage(ChatColor.RED + "There are more than 30 players on that IP Address! If you need the list message Legobuilder0813 on Slack.");
                return;
            }
            PacketIPSeenCommand packet = new PacketIPSeenCommand(player.getUniqueId(), users, args[0]);
            player.send(packet);
        });
    }
}