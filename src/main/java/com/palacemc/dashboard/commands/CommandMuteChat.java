package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.Collections;

public class CommandMuteChat extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandMuteChat() {
        super(Rank.SQUIRE);
        aliases = Collections.singletonList("chatmute");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
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
                tp.sendMessage(tp.getRank().getRankId() >= Rank.SQUIRE.getRankId() ? msgname : msg);
            }
        }
    }
}