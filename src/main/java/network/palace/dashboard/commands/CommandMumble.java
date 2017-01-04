package network.palace.dashboard.commands;

import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.packets.dashboard.PacketLink;

/**
 * Created by Marc on 9/22/16
 */
public class CommandMumble extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketLink packet = new PacketLink(player.getUniqueId(), "https://palace.network/mumble/",
                "Click for more information on Mumble", ChatColor.YELLOW, true);
        player.send(packet);
    }
}