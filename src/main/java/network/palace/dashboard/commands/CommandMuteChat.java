package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Arrays;

public class CommandMuteChat extends MagicCommand {

    public CommandMuteChat() {
        super(Rank.SQUIRE);
        aliases = Arrays.asList("chatmute");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        String server = player.getServer();
        if (Dashboard.getServer(server).isPark()) {
            server = "ParkChat";
        }
        boolean muted = Dashboard.chatUtil.isChatMuted(server);
        String msg = "";
        if (muted) {
            Dashboard.chatUtil.unmuteChat(server);
            msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                    ChatColor.YELLOW + "Chat has been unmuted";
        } else {
            Dashboard.chatUtil.muteChat(server);
            msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                    ChatColor.YELLOW + "Chat has been muted";
        }
        String msgname = msg + " by " + player.getName();
        for (Player tp : Dashboard.getOnlinePlayers()) {
            if ((server.equals("ParkChat") && Dashboard.getServer(tp.getServer()).isPark()) || tp.getServer().equals(server)) {
                tp.sendMessage(tp.getRank().getRankId() >= Rank.SQUIRE.getRankId() ? msgname : msg);
            }
        }
    }
}