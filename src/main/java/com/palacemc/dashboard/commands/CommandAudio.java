package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.audio.PacketContainer;
import com.palacemc.dashboard.packets.audio.PacketKick;
import com.palacemc.dashboard.packets.dashboard.PacketAudioCommand;
import com.palacemc.dashboard.packets.dashboard.PacketConnectionType;
import com.palacemc.dashboard.server.DashboardSocketChannel;

import java.util.List;
import java.util.Random;

public class CommandAudio extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length != 0) {
            switch (args[0].toLowerCase()) {
                case "leave":
                case "disconnect":
                    List<DashboardSocketChannel> list = dashboard.getChannels(PacketConnectionType.ConnectionType.AUDIOSERVER);

                    if (!list.isEmpty()) {
                        PacketKick packet = new PacketKick("You were disconnected!");
                        PacketContainer container = new PacketContainer(player.getUuid(), packet.getJSON().toString());

                        for (DashboardSocketChannel ch : list) {
                            ch.send(container);
                        }
                    }
                    break;
                default:
                    player.sendMessage(ChatColor.GREEN + "Audio Server Commands:\n" + ChatColor.AQUA + "- /audio " +
                            ChatColor.GREEN + "- Connect to Audio Server\n" + ChatColor.AQUA + "- /audio [leave/disconnect] " +
                            ChatColor.GREEN + "- Disconnect from the Audio Server\n" + ChatColor.AQUA + "- /audio help " +
                            ChatColor.GREEN + "- View this help menu");
                    break;
            }
            return;
        }

        player.setAudioAuth(new Random().nextInt(100000));
        PacketAudioCommand packet = new PacketAudioCommand(player.getUuid(), player.getAudioAuth());
        player.send(packet);
    }
}