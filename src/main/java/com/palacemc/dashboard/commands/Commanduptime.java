package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketUptimeCommand;
import com.palacemc.dashboard.handlers.MagicCommand;

/**
 * Created by Marc on 8/26/16
 */
public class Commanduptime extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketUptimeCommand packet = new PacketUptimeCommand(player.getUniqueId(), Dashboard.getStartTime());
        player.send(packet);
    }
}