package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;

public class CommandDoc extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(ChatColor.GREEN + "\nTotal Players Online: " + Dashboard.getOnlinePlayers().size() + "\n");
    }
}