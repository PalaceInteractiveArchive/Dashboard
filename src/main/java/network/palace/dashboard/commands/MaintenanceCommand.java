package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.packets.dashboard.PacketMaintenance;
import network.palace.dashboard.packets.dashboard.PacketMaintenanceWhitelist;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by Marc on 9/12/16
 */
public class MaintenanceCommand extends DashboardCommand {

    public MaintenanceCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.setMaintenance(!dashboard.isMaintenance());
        PacketMaintenance packet = new PacketMaintenance(dashboard.isMaintenance());
        if (dashboard.isMaintenance()) {
            List<UUID> staff = dashboard.getMongoHandler().getPlayersByRank(Rank.TRAINEE, Rank.TRAINEEBUILD,
                    Rank.MOD, Rank.BUILDER, Rank.ARCHITECT, Rank.SRMOD, Rank.DEVELOPER, Rank.ADMIN, Rank.MANAGER, Rank.DIRECTOR);
            dashboard.setMaintenanceWhitelist(staff);
            PacketMaintenanceWhitelist whitelist = new PacketMaintenanceWhitelist(staff);
            player.sendMessage(ChatColor.GREEN + "Maintenance Mode enabled! Notifying Bungees...");
            for (Object o : WebSocketServerHandler.getGroup()) {
                DashboardSocketChannel bungee = (DashboardSocketChannel) o;
                if (!bungee.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                    continue;
                }
                bungee.send(packet);
                bungee.send(whitelist);
            }
            player.sendMessage(ChatColor.GREEN + "Bungees notified! Disconnecting all Guests...");
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                    continue;
                }
                tp.kickPlayer(ChatColor.AQUA + "Maintenance Mode has been enabled!\nFollow " + ChatColor.BLUE +
                        "@PalaceDev " + ChatColor.AQUA + "on Twitter for updates.");
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId()) return;
                    }
                    player.sendMessage(ChatColor.GREEN + "All Guests have been disconnected!");
                    cancel();
                }
            }, 0L, 1000L);
        } else {
            player.sendMessage(ChatColor.GREEN + "Maintenance Mode " + ChatColor.RED + "disabled! " +
                    ChatColor.GREEN + "Notifying Bungees...");
            for (Object o : WebSocketServerHandler.getGroup()) {
                DashboardSocketChannel bungee = (DashboardSocketChannel) o;
                if (!bungee.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                    continue;
                }
                bungee.send(packet);
            }
            player.sendMessage(ChatColor.GREEN + "Bungees notified!");
        }
    }
}