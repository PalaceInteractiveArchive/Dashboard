package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

public class CommandIP extends MagicCommand {

    public CommandIP() {
        super(Rank.SQUIRE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "/ip [Player]");
            return;
        }
        Player tp = Dashboard.getPlayer(args[0]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + "That player wasn't found!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "IP of " + tp.getName() + " is " + tp.getAddress());
    }
}