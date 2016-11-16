package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Rank;

import java.util.Arrays;
import java.util.UUID;

public class CommandUnban extends MagicCommand {

    public CommandUnban() {
        super(Rank.KNIGHT);
        aliases = Arrays.asList("pardon");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unban [Username]");
            return;
        }
        String username = args[0];
        UUID uuid = Dashboard.sqlUtil.uuidFromUsername(username);
        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        Dashboard.sqlUtil.unbanPlayer(uuid);
        Dashboard.moderationUtil.announceUnban(username, player.getName());
    }
}