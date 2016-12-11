package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

public class CommandSC extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

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
            player = dashboard.getPlayer(player.getUuid());
            Rank rank = player.getRank();
            msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " + rank.getNameWithBrackets()
                    + " " + ChatColor.GRAY + player.getUsername() + ": " + ChatColor.WHITE +
                    ChatColor.translateAlternateColorCodes('&', message);

            dashboard.getChatUtil().staffChatMessage(msg);
            if (player != null) {
                dashboard.getChatUtil().logMessage(player.getUuid(), "/sc " + player.getUsername() + " " + message);
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "/sc [Message]");
    }
}