package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;

/**
 * Created by Marc on 12/12/16.
 */
public class CommandLink extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard.forum.linkAccount(player);
    }
}
