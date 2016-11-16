package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
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
        boolean enabled = Dashboard.chatUtil.privateMessagesEnabled();
        Dashboard.chatUtil.setPrivateMessages(!enabled);
        Dashboard.moderationUtil.togglePrivate(Dashboard.chatUtil.privateMessagesEnabled(), player.getName());
    }
}