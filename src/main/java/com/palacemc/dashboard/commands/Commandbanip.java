package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.*;

public class Commandbanip extends MagicCommand {

    public Commandbanip() {
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
        Dashboard.sqlUtil.banIP(ip, reason, player.getName());
        if (!ip.contains("*")) {
            for (Player tp : Dashboard.getOnlinePlayers()) {
                if (tp.getAddress().equals(ip)) {
                    try {
                        tp.kickPlayer(ChatColor.RED + "Your IP Has Been Banned For " + ChatColor.AQUA + reason);
                    } catch (Exception ignored) {
                    }
                }
            }
        } else {
            for (Player tp : Dashboard.getOnlinePlayers()) {
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
        Dashboard.moderationUtil.announceBan(new AddressBan(ip, reason, player.getName()));
    }
}