package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;

/**
 * Created by Marc on 8/20/16
 */
public class ModerationUtil {

    public void announceBan(Ban ban) {
        sendMessage(ChatColor.GREEN + ban.getName() + ChatColor.RED + " was banned by " + ChatColor.GREEN +
                ban.getSource() + ChatColor.RED + " Reason: " + ChatColor.GREEN + ban.getReason() +
                ChatColor.RED + " Expires: " + ChatColor.GREEN + (ban.isPermanent() ? "Permanent" :
                DateUtil.formatDateDiff(ban.getRelease())));
    }

    public void announceBan(AddressBan ban) {
        sendMessage(ChatColor.GREEN + "IP " + ban.getAddress() + ChatColor.RED + " was banned by " + ChatColor.GREEN +
                ban.getSource() + ChatColor.RED + " Reason: " + ChatColor.GREEN + ban.getReason());
    }

    public void announceUnban(String name, String source) {
        sendMessage(ChatColor.GREEN + name + ChatColor.RED + " has been unbanned by " + ChatColor.GREEN + source);
    }

    public void announceKick(String name, String reason, String source) {
        sendMessage(ChatColor.GREEN + name + ChatColor.RED + " was kicked by " + ChatColor.GREEN + source +
                ChatColor.RED + " Reason: " + ChatColor.GREEN + reason);
    }

    public void announceMute(Mute mute) {
        sendMessage(ChatColor.GREEN + mute.getName() + ChatColor.RED + " was muted by " + ChatColor.GREEN +
                mute.getSource() + ChatColor.RED + " Reason: " + ChatColor.GREEN + mute.getReason() + ChatColor.RED +
                " Expires: " + ChatColor.GREEN + DateUtil.formatDateDiff(mute.getRelease()));
    }

    public void announceUnmute(String name, String source) {
        sendMessage(ChatColor.GREEN + name + ChatColor.RED + " has been unmuted by " + ChatColor.RED + source);
    }


    public void changeChatDelay(int time, String source) {
        sendMessage(ChatColor.GREEN + "The chat delay was set to " + time + " seconds by " + source);
    }

    public void rankChange(String name, Rank rank, String source) {
        sendMessage(ChatColor.GREEN + name + "'s rank has been changed to " + rank.getNameWithBrackets() +
                ChatColor.GREEN + " by " + source);
    }

    public void sendMessage(String message) {
        String msg = ChatColor.WHITE + "[" + ChatColor.RED + "Dashboard" + ChatColor.WHITE + "] " + message;
        for (Player player : Launcher.getDashboard().getOnlinePlayers()) {
            if (player.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                player.sendMessage(msg);
            }
        }
    }

    public void togglePrivate(boolean enabled, String name) {
        sendMessage(ChatColor.GREEN + "Private messages have been " + (enabled ? "enabled" : ChatColor.RED +
                "disabled" + ChatColor.GREEN) + " by " + name);
    }
}