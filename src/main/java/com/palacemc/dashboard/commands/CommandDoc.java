package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;

public class CommandDoc extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(
                ChatColor.GREEN + "\nTotal Players Online: " + Launcher.getDashboard().getOnlinePlayers().size() + "\n");
    }
}