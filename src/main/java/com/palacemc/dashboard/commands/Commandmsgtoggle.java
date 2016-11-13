package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

public class Commandmsgtoggle extends MagicCommand {

    public Commandmsgtoggle() {
        super(Rank.WIZARD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.setRecieveMessages(!player.canRecieveMessages());
        if (player.canRecieveMessages()) {
            player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.GREEN + "enabled " + ChatColor.YELLOW +
                    "receiving private messages!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.RED + "disabled " + ChatColor.YELLOW +
                    "receiving private messages!");
        }
    }
}