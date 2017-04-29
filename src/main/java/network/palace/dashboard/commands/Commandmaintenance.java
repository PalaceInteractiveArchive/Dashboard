package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.packets.dashboard.PacketMaintenance;
import network.palace.dashboard.packets.dashboard.PacketMaintenanceWhitelist;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.util.*;

/**
 * Created by Marc on 9/12/16
 */
public class Commandmaintenance extends MagicCommand {

    public Commandmaintenance() {
        super(Rank.WIZARD);
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.setMaintenance(!dashboard.isMaintenance());
        PacketMaintenance packet = new PacketMaintenance(dashboard.isMaintenance());
        if (dashboard.isMaintenance()) {
            HashMap<Rank, List<UUID>> staff = dashboard.getSqlUtil().getPlayersByRanks(Rank.SQUIRE, Rank.KNIGHT,
                    Rank.PALADIN, Rank.WIZARD, Rank.EMPEROR, Rank.EMPRESS);
            List<UUID> list = new ArrayList<>();
            for (Map.Entry<Rank, List<UUID>> entry : staff.entrySet()) {
                for (UUID uuid : entry.getValue()) {
                    list.add(uuid);
                }
            }
            dashboard.setMaintenanceWhitelist(list);
            PacketMaintenanceWhitelist whitelist = new PacketMaintenanceWhitelist(list);
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
                if (tp.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                    continue;
                }
                tp.kickPlayer(ChatColor.AQUA + "Maintenance Mode has been enabled!\nFollow " + ChatColor.BLUE +
                        "@PalaceDev " + ChatColor.AQUA + "on Twitter for updates.");
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    boolean guests = false;
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
                            guests = true;
                            return;
                        }
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