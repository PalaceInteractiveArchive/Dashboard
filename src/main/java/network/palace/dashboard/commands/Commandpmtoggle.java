package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

/**
 * Created by Marc on 10/8/16
 */
public class Commandpmtoggle extends MagicCommand {

    public Commandpmtoggle() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        boolean enabled = Dashboard.chatUtil.privateMessagesEnabled();
        Dashboard.chatUtil.setPrivateMessages(!enabled);
        Dashboard.moderationUtil.togglePrivate(Dashboard.chatUtil.privateMessagesEnabled(), player.getName());
    }
}