package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;

public class CommandKick extends MagicCommand {

    public CommandKick() {
        super(Rank.SQUIRE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/kick [Player] [Reason]");
            return;
        }

        Player tp = Launcher.getDashboard().getPlayer(args[0]);

        if (tp == null) {
            player.sendMessage(ChatColor.RED + "I can't find that player!");
            return;
        }

        if (tp.isKicking()) {
            player.sendMessage(ChatColor.RED + "I can't find that player!");
            return;
        }

        String r = "";

        for (int i = 1; i < args.length; i++) {
            r += args[i] + " ";
        }

        String reason = r.substring(0, 1).toUpperCase() + r.substring(1);
        reason = reason.trim();

        tp.kickPlayer(ChatColor.RED + "You have been disconnected for: " + ChatColor.AQUA + reason);

        try {
            Launcher.getDashboard().getModerationUtil().announceKick(tp.getUsername(), reason, player.getUsername());
            Launcher.getDashboard().getSqlUtil().logKick(new Kick(tp.getUuid(), reason, player.getUsername()));
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "That player isn't online!");
        }
    }
}