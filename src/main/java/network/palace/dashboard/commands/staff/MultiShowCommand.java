package network.palace.dashboard.commands.staff;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ClickEvent;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.audio.PacketContainer;
import network.palace.dashboard.packets.audio.PacketKick;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.packets.park.PacketShowStart;
import network.palace.dashboard.packets.park.PacketShowStop;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.util.ArrayList;
import java.util.List;

public class MultiShowCommand extends DashboardCommand {

    public MultiShowCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 3) {
            Dashboard dashboard = Launcher.getDashboard();
            List<String> servers = new ArrayList<>();
            String showName = args[1];
            String server = args[2];
            if (exists(server)) {
                switch (args[0].toLowerCase()) {
                    case "start":
                        PacketShowStart startPacket = new PacketShowStart(args[1]);
                        for (Object o : WebSocketServerHandler.getGroup()) {
                            DashboardSocketChannel dash = (DashboardSocketChannel) o;
                            if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) continue;
                            if (dash.getServerName().startsWith(server)) {
                                dash.send(startPacket);
                                servers.add(dash.getServerName());
                            }
                        }
                        dashboard.getModerationUtil().sendMessage(ChatColor.translateAlternateColorCodes('&', "&aAttempting to start &b" + showName + " &aon " + String.join("&a, &b", servers) + "&a."));
                    case "stop":
                        PacketShowStop stopPacket = new PacketShowStop(args[1]);
                        for (Object o : WebSocketServerHandler.getGroup()) {
                            DashboardSocketChannel dash = (DashboardSocketChannel) o;
                            if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) continue;
                            if (dash.getServerName().startsWith(server)) {
                                dash.send(stopPacket);
                                servers.add(dash.getServerName());
                            }
                        }
                        dashboard.getModerationUtil().sendMessage(ChatColor.translateAlternateColorCodes('&', "&aAttempting to stop &b" + showName + " &aon " + String.join("&a, &b", servers) + "&a."));
                    default:
                        player.sendMessage(ChatColor.GREEN + "MultiShow Commands:\n" + ChatColor.AQUA + "- /multishow start [show] [server] " +
                                ChatColor.GREEN + "- Starts the specified show on all instances of the specified server\n" + ChatColor.AQUA + "- /multishow stop [show] [server] " +
                                ChatColor.GREEN + "- Stops the specified show on all instances of the specified server\n" + ChatColor.AQUA + "- /multishow help " +
                                ChatColor.GREEN + "- View this help menu");
                        break;
                }
            } else {
                player.sendMessage(ChatColor.RED + "Please provide a valid server without the server number (MK not MK3).");
            }
            return;
        }
        player.sendMessage(ChatColor.GREEN + "MultiShow Commands:\n" + ChatColor.AQUA + "- /multishow start [show] [server] " +
                ChatColor.GREEN + "- Starts the specified show on all instances of the specified server\n" + ChatColor.AQUA + "- /multishow stop [show] [server] " +
                ChatColor.GREEN + "- Stops the specified show on all instances of the specified server\n" + ChatColor.AQUA + "- /multishow help " +
                ChatColor.GREEN + "- View this help menu");
    }

    private boolean exists(String s) {
        for (String server : Launcher.getDashboard().getJoinServers()) {
            if (server.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
}