package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

public class CommandCC extends MagicCommand {
    public String clearMessage = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandCC() {
        super(Rank.SQUIRE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        String server = player.getServer();
        boolean park = dashboard.getServer(server).isPark();

        for (Player tp : dashboard.getOnlinePlayers()) {
            if (tp.getServer().equals(server) || (park && dashboard.getServer(tp.getServer()).isPark())) {
                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
                    tp.sendMessage(clearMessage + ChatColor.DARK_AQUA + "Chat has been cleared");
                } else {
                    tp.sendMessage("\n" + ChatColor.DARK_AQUA + "Chat has been cleared by " + player.getUsername());
                }
            }
        }
    }
}