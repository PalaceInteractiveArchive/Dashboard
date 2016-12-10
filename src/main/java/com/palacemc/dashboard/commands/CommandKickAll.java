package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Marc on 10/3/16
 */
public class CommandKickAll extends MagicCommand {

    public CommandKickAll() {
        super(Rank.WIZARD);
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        if (args.length <= 0) {
            player.sendMessage(ChatColor.RED + "/kickall [reason]");
            return;
        }

        String r = "";

        for (String arg : args) {
            r += arg + " ";
        }

        r = ChatColor.translateAlternateColorCodes('&', r.trim());
        player.sendMessage(ChatColor.GREEN + "Disconnecting all players for " + r);

        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            if (tp.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                continue;
            }
            tp.kickPlayer(r);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                boolean empty = true;
                for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
                    if (tp.getRank().getRankId() < Rank.WIZARD.getRankId()) {
                        empty = false;
                        break;
                    }
                }

                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "All players have been disconnected!");
                    cancel();
                }
            }
        }, 0, 1000);
    }
}