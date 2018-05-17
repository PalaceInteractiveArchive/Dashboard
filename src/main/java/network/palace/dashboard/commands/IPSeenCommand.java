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
            List<String> users = dashboard.getMongoHandler().getPlayersOnIP(args[0]);
            if (users == null || users.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No users found on that IP Address.");
                return;
            }
            PacketIPSeenCommand packet = new PacketIPSeenCommand(player.getUniqueId(), users, args[0]);
            player.send(packet);
        });
    }
}