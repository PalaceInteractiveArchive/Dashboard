package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketStartReboot;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.util.Timer;
import java.util.TimerTask;

public class Commandreboot extends MagicCommand {

    public Commandreboot() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        player.sendMessage(ChatColor.GREEN + "Starting shutdown of Dashboard...");
        PacketStartReboot packet = new PacketStartReboot();
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel bungee = (DashboardSocketChannel) o;
            bungee.send(packet);
        }
        player.sendMessage(ChatColor.GREEN + "Bungees notified, disconnecting " + dashboard.getOnlinePlayers().size()
                + " players...");
        for (Player tp : dashboard.getOnlinePlayers()) {
            tp.kickPlayer(ChatColor.AQUA + "Please Pardon our Pixie Dust! We are restarting our servers right now.");
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (dashboard.getOnlinePlayers().isEmpty()) {
                    cancel();
                    System.exit(0);
                }
            }
        }, 1000);
    }
}