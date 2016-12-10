package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketLink;

/**
 * Created by Marc on 9/27/16
 */
public class CommandBug extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketLink packet = new PacketLink(player.getUniqueId(), "https://goo.gl/sMMiYZ", "Click to report a bug",
                ChatColor.YELLOW, true);

        player.send(packet);
    }
}