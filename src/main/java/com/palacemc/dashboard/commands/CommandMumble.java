package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.packets.dashboard.PacketLink;

/**
 * Created by Marc on 9/22/16
 */
public class CommandMumble extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketLink packet = new PacketLink(player.getUuid(), "https://palace.network/mumble/",
                "Click for more information on Mumble", ChatColor.YELLOW, true);
        player.send(packet);
    }
}