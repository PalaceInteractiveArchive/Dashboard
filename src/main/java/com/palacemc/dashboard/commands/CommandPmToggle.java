package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

/**
 * Created by Marc on 10/8/16
 */
public class CommandPmToggle extends MagicCommand {

    public CommandPmToggle() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        boolean enabled = Launcher.getDashboard().getChatUtil().privateMessagesEnabled();

        Launcher.getDashboard().getChatUtil().setPrivateMessages(!enabled);

        Launcher.getDashboard().getModerationUtil().togglePrivate(
                Launcher.getDashboard().getChatUtil().privateMessagesEnabled(), player.getName());
    }
}