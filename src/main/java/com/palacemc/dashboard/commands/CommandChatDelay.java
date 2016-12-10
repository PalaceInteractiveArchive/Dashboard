package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

public class CommandChatDelay extends MagicCommand {

    public CommandChatDelay() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "The chat delay is currently " +
                    (Launcher.getDashboard().getChatUtil().getChatDelay() / 1000) + " seconds!");
            player.sendMessage(ChatColor.GREEN + "Change delay: /chatdelay [Time]");
            return;
        }

        try {
            int time = Integer.parseInt(args[0]);
            Launcher.getDashboard().getChatUtil().setChatDelay(time * 1000);
            Launcher.getDashboard().getModerationUtil().changeChatDelay(time, player.getName());
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please use a whole number");
        }
    }
}