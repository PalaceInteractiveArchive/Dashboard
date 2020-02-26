package network.palace.dashboard.commands;

import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketLink;

/**
 * Created by Marc on 9/27/16
 */
public class BugCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketLink packet = new PacketLink(player.getUniqueId(), "https://palnet.us/bugreport", "Click to report a bug",
                ChatColor.YELLOW, true);
        player.send(packet);
    }
}