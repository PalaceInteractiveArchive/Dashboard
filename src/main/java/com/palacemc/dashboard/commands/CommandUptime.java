package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketUptimeCommand;

/**
 * Created by Marc on 8/26/16
 */
public class CommandUptime extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketUptimeCommand packet = new PacketUptimeCommand(
                player.getUniqueId(), Launcher.getDashboard().getStartTime());
        player.send(packet);
    }
}