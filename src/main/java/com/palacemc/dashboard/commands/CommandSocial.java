package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.packets.dashboard.PacketLink;

public class CommandSocial extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "MCMagic Social Links:");
        player.send(new PacketLink(player.getUniqueId(), "https://mcmagic.us", "Website: https://mcmagic.us",
                ChatColor.GREEN, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://mcmagic.us/mumble",
                "Mumble: https://mcmagic.us/mumble", ChatColor.YELLOW, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://twitter.com/MCMagicParks",
                "Twitter: @MCMagicParks", ChatColor.AQUA, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://beam.pro/MCMagicParks",
                "Beam: https://beam.pro/MCMagicParks", ChatColor.DARK_PURPLE, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://youtube.com/MCMagicParks",
                "YouTube: https://youtube.com/MCMagicParks", ChatColor.RED, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://facebook.com/MCMagicParks",
                "Facebook: https://facebook.com/MCMagicParks", ChatColor.BLUE, false, false));
        player.send(new PacketLink(player.getUniqueId(), "https://instagram.com/MCMagicParks",
                "Instagram: https://instagram.com/MCMagicParks", ChatColor.LIGHT_PURPLE, false, false));
    }
}