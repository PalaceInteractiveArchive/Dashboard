package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.Arrays;

public class CommandUnbanIP extends MagicCommand {

    public CommandUnbanIP() {
        super(Rank.KNIGHT);
        aliases = Arrays.asList("pardonip", "pardon-ip");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unbanip [IP Address]");
            return;
        }

        String address = args[0];
        Launcher.getDashboard().getSqlUtil().unbanIP(address);
        Launcher.getDashboard().getModerationUtil().announceUnban("IP " + address, player.getName());
    }
}