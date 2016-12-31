package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.UUID;

public class CommandUnmute extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

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
        Player dashboardPlayer = dashboard.getPlayer(username);
        UUID uuid;

        if (dashboardPlayer == null) {
            uuid = dashboard.getSqlUtil().uuidFromUsername(username);
        } else {
            uuid = dashboardPlayer.getUuid();
            username = dashboardPlayer.getUsername();
        }

        if (uuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        dashboard.getSqlUtil().unmutePlayer(uuid);

        if (dashboardPlayer == null) return;

        dashboardPlayer.getMute().setMuted(false);
        dashboard.getModerationUtil().announceUnmute(username, player.getUsername());
    }
}