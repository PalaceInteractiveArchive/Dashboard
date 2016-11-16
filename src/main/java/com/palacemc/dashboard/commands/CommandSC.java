package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Rank;

public class CommandSC extends MagicCommand {

    public CommandSC() {
        super(Rank.SQUIRE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length > 0) {
            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }
            String msg;
            player = Dashboard.getPlayer(player.getUniqueId());
            Rank rank = player.getRank();
            msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " + rank.getNameWithBrackets()
                    + " " + ChatColor.GRAY + player.getName() + ": " + ChatColor.WHITE +
                    ChatColor.translateAlternateColorCodes('&', message);
            Dashboard.chatUtil.staffChatMessage(msg);
            if (player != null) {
                Dashboard.chatUtil.logMessage(player.getUniqueId(), "/sc " + player.getName() + " " + message);
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "/sc [Message]");
    }
}