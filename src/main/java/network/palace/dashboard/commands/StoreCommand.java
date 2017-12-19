package network.palace.dashboard.commands;

import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketLink;

/**
 * Created by Marc on 9/22/16
 */
public class StoreCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketLink packet = new PacketLink(player.getUniqueId(), "https://store.palace.network",
                "Click to visit our store", ChatColor.YELLOW, true);
        player.send(packet);
    }
}