package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.Arrays;

public class CommandMuteChat extends MagicCommand {

    public CommandMuteChat() {
        super(Rank.SQUIRE);
        aliases = Arrays.asList("chatmute");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        String server = player.getServer();

        if (Launcher.getDashboard().getServer(server).isPark()) {
            server = "ParkChat";
        }

        boolean muted = Launcher.getDashboard().getChatUtil().isChatMuted(server);
        String msg;

        if (muted) {
            Launcher.getDashboard().getChatUtil().unmuteChat(server);
            msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                    ChatColor.YELLOW + "Chat has been unmuted";
        } else {
            Launcher.getDashboard().getChatUtil().muteChat(server);
            msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                    ChatColor.YELLOW + "Chat has been muted";
        }

        String msgname = msg + " by " + player.getName();

        for (Player tp : Launcher.getDashboard().getOnlinePlayers()) {
            if ((server.equals("ParkChat") && Launcher.getDashboard().getServer(tp.getServer()).isPark()) || tp.getServer().equals(server)) {
                tp.sendMessage(tp.getRank().getRankId() >= Rank.SQUIRE.getRankId() ? msgname : msg);
            }
        }
    }
}