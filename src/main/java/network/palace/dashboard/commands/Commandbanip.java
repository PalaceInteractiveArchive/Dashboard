package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;

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
        StringBuilder r = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            r.append(args[i]).append(" ");
        }
        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        String finalReason = reason.trim();
        Dashboard.schedulerManager.runAsync(() -> {
            AddressBan existing = Dashboard.sqlUtil.getAddressBan(ip);
            if (existing != null) {
                player.sendMessage(ChatColor.RED + "This IP " + (!ip.contains("*") ? "Address " : "Range ") +
                        "is already banned! Unban it to change the reason.");
                return;
            }
            Dashboard.sqlUtil.banIP(ip, finalReason, player.getName());
            if (!ip.contains("*")) {
                for (Player tp : Dashboard.getOnlinePlayers()) {
                    if (tp.getAddress().equals(ip)) {
                        try {
                            tp.kickPlayer(ChatColor.RED + "Your IP Has Been Banned For " + ChatColor.AQUA + finalReason);
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
                            tp.kickPlayer(ChatColor.RED + "Your IP Range Has Been Banned For " + ChatColor.AQUA + finalReason);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            Dashboard.moderationUtil.announceBan(new AddressBan(ip, finalReason, player.getName()));
        });
    }
}