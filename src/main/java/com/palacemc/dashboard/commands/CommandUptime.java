package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketUptimeCommand;

/**
 * Created by Marc on 8/26/16
 */
public class CommandUptime extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketUptimeCommand packet = new PacketUptimeCommand(
                player.getUuid(), dashboard.getStartTime());
        player.send(packet);
    }
}