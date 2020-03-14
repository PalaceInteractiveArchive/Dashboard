package network.palace.dashboard.commands.moderation;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Collections;

public class MuteChatCommand extends DashboardCommand {

    public MuteChatCommand() {
        super(Rank.TRAINEE);
        aliases = Collections.singletonList("chatmute");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        String server = player.getServer();
        if (dashboard.getServer(server).isPark()) {
            server = "ParkChat";
        }
        boolean muted = dashboard.getChatUtil().isChatMuted(server);
        String msg;
        if (muted) {
            dashboard.getChatUtil().unmuteChat(server);
            msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                    ChatColor.YELLOW + "Chat has been unmuted";
        } else {
            dashboard.getChatUtil().muteChat(server);
            msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                    ChatColor.YELLOW + "Chat has been muted";
        }
        String msgname = msg + " by " + player.getUsername();
        for (Player tp : dashboard.getOnlinePlayers()) {
            if ((server.equals("ParkChat") && dashboard.getServer(tp.getServer()).isPark()) || tp.getServer().equals(server)) {
                tp.sendMessage(tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() ? msgname : msg);
            }
        }
    }
}