package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ClickEvent;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.audio.PacketContainer;
import network.palace.dashboard.packets.audio.PacketKick;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.server.DashboardSocketChannel;

import java.util.List;

public class AudioCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length != 0) {
            switch (args[0].toLowerCase()) {
                case "leave":
                case "disconnect":
                    List<DashboardSocketChannel> list = Dashboard.getChannels(PacketConnectionType.ConnectionType.AUDIOSERVER);
                    if (!list.isEmpty()) {
                        PacketKick packet = new PacketKick("You were disconnected!");
                        PacketContainer container = new PacketContainer(player.getUniqueId(), packet.getJSON().toString());
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
        player.sendMessage(new ComponentBuilder("\nClick here to connect to our Audio Server!\n")
                .color(ChatColor.GREEN).underlined(true).bold(true)
                .event((new ClickEvent(ClickEvent.Action.OPEN_URL,
                        (Launcher.getDashboard().isTestNetwork() ? "https://audio-test.palace.network/?t=" :
                                "https://audio.palace.network/?t=") + player.setAudioToken()))).create());
    }
}