package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.packets.dashboard.PacketStartReboot;
import com.palacemc.dashboard.server.DashboardSocketChannel;
import com.palacemc.dashboard.server.WebSocketServerHandler;

import java.util.Timer;
import java.util.TimerTask;

public class CommandReboot extends MagicCommand {

    public CommandReboot() {
        super(Rank.WIZARD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Starting shutdown of Dashboard...");

        PacketStartReboot packet = new PacketStartReboot();

        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel bungee = (DashboardSocketChannel) o;
            bungee.send(packet);
        }

        player.sendMessage(ChatColor.GREEN + "Bungees notified, disconnecting " + Launcher.getDashboard().getOnlinePlayers().size()
                + " players...");

        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            tp.kickPlayer(ChatColor.AQUA + "Mind the dust! We are restarting our servers right now.");
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (Launcher.getDashboard().getOnlinePlayers().isEmpty()) {
                    cancel();
                    System.exit(0);
                }
            }
        }, 1000);
    }
}