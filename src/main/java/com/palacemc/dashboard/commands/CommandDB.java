package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

public class CommandDB extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandDB() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length > 0) {
            String message = "";

            for (String arg : args) {
                message += arg + " ";
            }

            String sname = player.getUsername();

            String msg = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" +
                    ChatColor.WHITE + "] " + ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);

            String staff = ChatColor.WHITE + "[" + ChatColor.AQUA +
                    sname + ChatColor.WHITE + "] " + ChatColor.GREEN +
                    ChatColor.translateAlternateColorCodes('&', message);

            for (Player tp : dashboard.getOnlinePlayers()) {
                if (dashboard.getPlayer(tp.getUuid()).getRank().getRankId() >= Rank.KNIGHT.getRankId()) {
                    tp.sendMessage(staff);
                } else {
                    tp.sendMessage(msg);
                }
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "/b [Message]");
    }
}