package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketUptimeCommand;

/**
 * Created by Marc on 8/26/16
 */
public class Commanduptime extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        PacketUptimeCommand packet = new PacketUptimeCommand(player.getUniqueId(), Launcher.getDashboard().getStartTime());
        player.send(packet);
    }
}