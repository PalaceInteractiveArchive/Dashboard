package network.palace.dashboard.commands;

import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketLink;

public class ApplyCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketLink packet = new PacketLink(player.getUniqueId(), "https://palnet.us/apply", "Click here to apply",
                ChatColor.YELLOW, true);
        player.send(packet);
    }
}
