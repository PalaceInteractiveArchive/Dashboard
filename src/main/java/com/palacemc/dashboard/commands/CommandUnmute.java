package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.UUID;

public class CommandUnmute extends MagicCommand {

    public CommandUnmute() {
        super(Rank.KNIGHT);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/unmute [Username]");
            return;
        }

        String username = args[0];
        Player tp = Launcher.getDashboard().getPlayer(username);
        UUID uuid;

        if (tp == null) {
            uuid = Launcher.getDashboard().getSqlUtil().uuidFromUsername(username);
        } else {
            uuid = tp.getUuid();
            username = tp.getUsername();
        }

        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        Launcher.getDashboard().getSqlUtil().unmutePlayer(uuid);
        tp.getMute().setMuted(false);
        Launcher.getDashboard().getModerationUtil().announceUnmute(username, player.getUsername());
    }
}