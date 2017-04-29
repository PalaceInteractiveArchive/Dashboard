package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
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
        Dashboard dashboard = Launcher.getDashboard();
        boolean enabled = dashboard.getChatUtil().privateMessagesEnabled();
        dashboard.getChatUtil().setPrivateMessages(!enabled);
        dashboard.getModerationUtil().togglePrivate(dashboard.getChatUtil().privateMessagesEnabled(), player.getName());
    }
}