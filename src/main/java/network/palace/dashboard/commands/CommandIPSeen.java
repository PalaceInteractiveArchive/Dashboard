package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.AddressBan;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketIPSeenCommand;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;

import java.util.List;

public class CommandIPSeen extends MagicCommand {

    @Override
    public void execute(final Player player, String label, final String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/ipseen [IP Address]");
            return;
        }
        Dashboard.schedulerManager.runAsync(() -> {
            AddressBan ban = Dashboard.sqlUtil.getAddressBan(args[0]);
            if (ban != null) {
                player.sendMessage(ChatColor.RED + "This IP Address is banned for " + ChatColor.AQUA + ban.getReason());
            }
            List<String> users = Dashboard.sqlUtil.getNamesFromIP(args[0]);
            if (users == null || users.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No users found on that IP Address.");
                return;
            }
            PacketIPSeenCommand packet = new PacketIPSeenCommand(player.getUniqueId(), users, args[0]);
            player.send(packet);
        });
    }
}