package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Rank;

import java.util.Arrays;

public class Commandunbanip extends MagicCommand {

    public Commandunbanip() {
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
        Dashboard.sqlUtil.unbanIP(address);
        Dashboard.moderationUtil.announceUnban("IP " + address, player.getName());
    }
}