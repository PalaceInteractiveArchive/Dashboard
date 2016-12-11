package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;

public class CommandBanIP extends MagicCommand {

    public CommandBanIP() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/banip [IP Address] [Reason]");
            return;
        }

        String ip = args[0];
        String r = "";

        for (int i = 1; i < args.length; i++) {
            r += args[i] + " ";
        }

        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        reason = reason.trim();
        Launcher.getDashboard().getSqlUtil().banIP(ip, reason, player.getUsername());

        if (!ip.contains("*")) {
            for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                if (tp.getAddress().equals(ip)) {
                    try {
                        tp.kickPlayer(ChatColor.RED + "Your IP Has Been Banned For " + ChatColor.AQUA + reason);
                    } catch (Exception ignored) {
                    }
                }
            }
        } else {
            for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                String[] list = tp.getAddress().split("\\.");
                String range = list[0] + "." + list[1] + "." + list[2] + ".*";

                if (range.equalsIgnoreCase(ip)) {
                    try {
                        tp.kickPlayer(ChatColor.RED + "Your IP Range Has Been Banned For " + ChatColor.AQUA + reason);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        Launcher.getDashboard().getModerationUtil().announceBan(new AddressBan(ip, reason, player.getUsername()));
    }
}