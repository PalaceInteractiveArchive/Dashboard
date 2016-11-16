package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketConnectionType;
import com.palacemc.dashboard.server.WebSocketServerHandler;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.packets.dashboard.PacketUpdateMOTD;
import com.palacemc.dashboard.server.DashboardSocketChannel;

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
