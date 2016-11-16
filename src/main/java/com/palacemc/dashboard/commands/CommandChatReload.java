package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

/**
 * Created by Marc on 9/24/16
 */
public class CommandChatReload extends MagicCommand {

    public CommandChatReload() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "Reloading chat settings...");
        Dashboard.chatUtil.reload();
        Dashboard.schedulerManager.getBroadcastClock().reload();
        player.sendMessage(ChatColor.GREEN + "Chat settings reloaded!");
    }
}