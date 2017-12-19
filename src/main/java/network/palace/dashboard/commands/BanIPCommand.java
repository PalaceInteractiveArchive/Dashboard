package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

public class BanIPCommand extends DashboardCommand {

    public BanIPCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
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
        dashboard.getSchedulerManager().runAsync(() -> {
            AddressBan existing = dashboard.getSqlUtil().getAddressBan(ip);
            if (existing != null) {
                player.sendMessage(ChatColor.RED + "This IP " + (!ip.contains("*") ? "Address " : "Range ") +
                        "is already banned! Unban it to change the reason.");
                return;
            }
            dashboard.getSqlUtil().banIP(ip, finalReason, player.getUsername());
            if (!ip.contains("*")) {
                for (Player tp : dashboard.getOnlinePlayers()) {
                    if (tp.getAddress().equals(ip)) {
                        try {
                            tp.kickPlayer(ChatColor.RED + "Your IP Has Been Banned For " + ChatColor.AQUA + finalReason);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                for (Player tp : dashboard.getOnlinePlayers()) {
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
            dashboard.getModerationUtil().announceBan(new AddressBan(ip, finalReason, player.getUsername()));
        });
    }
}