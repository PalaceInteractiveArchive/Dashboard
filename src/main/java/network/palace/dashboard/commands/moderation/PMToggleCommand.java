package network.palace.dashboard.commands.moderation;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

/**
 * Created by Marc on 10/8/16
 */
public class PMToggleCommand extends DashboardCommand {

    public PMToggleCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        boolean enabled = dashboard.getChatUtil().privateMessagesEnabled();
        dashboard.getChatUtil().setPrivateMessages(!enabled);
        dashboard.getModerationUtil().togglePrivate(dashboard.getChatUtil().privateMessagesEnabled(), player.getUsername());
    }
}