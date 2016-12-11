package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

public class CommandHo extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandHo() {
        super(Rank.WIZARD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length > 0) {
            String message = "";

            for (String arg : args) {
                message += arg + " ";
            }

            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                    tp.sendMessage(ChatColor.RED + "[ADMIN CHAT] " + ChatColor.GRAY + player.getUsername() + ": " +
                            ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
                }
            }

            return;
        }
        player.sendMessage(ChatColor.RED + "/ho [Message]");
    }
}