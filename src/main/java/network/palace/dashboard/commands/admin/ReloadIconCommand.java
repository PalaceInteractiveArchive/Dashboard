package network.palace.dashboard.commands.admin;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.bungee.PacketServerIcon;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.server.DashboardSocketChannel;

/**
 * Created by Marc on 4/30/17.
 */
public class ReloadIconCommand extends DashboardCommand {

    public ReloadIconCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Calculating base64 hash and notifying bungees...");
        Dashboard dash = Launcher.getDashboard();
        String base64 = dash.getServerIconBase64();
        PacketServerIcon packet = new PacketServerIcon(base64);
        for (DashboardSocketChannel c : Dashboard.getChannels(PacketConnectionType.ConnectionType.BUNGEECORD)) {
            c.send(packet);
        }
        player.sendMessage(ChatColor.GREEN + "All server icons have been updated!");
    }
}
