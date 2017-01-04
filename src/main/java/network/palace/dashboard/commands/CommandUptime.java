package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketUptimeCommand;
import network.palace.dashboard.handlers.MagicCommand;

/**
 * Created by Marc on 8/26/16
 */
public class CommandUptime extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketUptimeCommand packet = new PacketUptimeCommand(player.getUniqueId(), Dashboard.getStartTime());
        player.send(packet);
    }
}