package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketLink;

public class CommandSocial extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Palace Network Social Links:");
        player.send(new PacketLink(player.getUniqueId(), "https://palace.network", "Website: https://palace.network",
                ChatColor.GREEN, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://palace.network/mumble",
                "Mumble: https://palace.network/mumble", ChatColor.YELLOW, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://twitter.com/PalaceNetwork",
                "Twitter: @PalaceNetwork", ChatColor.AQUA, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://beam.pro/PalaceNetworkMC",
                "Beam: https://beam.pro/PalaceNetwork", ChatColor.DARK_PURPLE, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://youtube.com/MCMagicParks",
                "YouTube: https://youtube.com/MCMagicParks", ChatColor.RED, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://facebook.com/PalaceNetworkMC",
                "Facebook: https://facebook.com/PalaceNetworkMC", ChatColor.BLUE, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://instagram.com/PalaceNetwork",
                "Instagram: https://instagram.com/PalaceNetwork", ChatColor.LIGHT_PURPLE, false, false));
    }
}