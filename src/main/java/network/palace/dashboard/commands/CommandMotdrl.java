package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.server.WebSocketServerHandler;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketUpdateMOTD;
import network.palace.dashboard.server.DashboardSocketChannel;

/**
 * Created by Marc on 8/26/16
 */
public class CommandMotdrl extends MagicCommand {

    public CommandMotdrl() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Loading MOTD from file...");
        Dashboard.loadMOTD();
        player.sendMessage(ChatColor.GREEN + "MOTD Loaded! Notifying Bungees...");
        PacketUpdateMOTD packet = new PacketUpdateMOTD(Dashboard.getMOTD(), Dashboard.getMOTDMaintenance(),
                Dashboard.getInfo());
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                continue;
            }
            dash.send(packet);
        }
        player.sendMessage(ChatColor.GREEN + "All Bungees have been notified!");
    }
}
