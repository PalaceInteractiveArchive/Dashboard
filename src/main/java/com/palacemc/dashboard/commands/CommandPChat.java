package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;
import com.palacemc.dashboard.utils.DateUtil;

import java.util.Date;

public class CommandPChat extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/pchat [Message]");
            return;
        }

        Party party = Launcher.getDashboard().getPartyUtil().findPartyForPlayer(player.getUniqueId());

        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }

        if (!enoughTime(player)) {
            player.sendMessage(ChatColor.RED + "New Guests must be on the server for at least 15 minutes before talking in chat. " +
                    ChatColor.DARK_AQUA + "Learn more at mcmagic.us/rules#chat");
            return;
        }

        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
            if (Launcher.getDashboard().getChatUtil().isMuted(player)) {
                return;
            }
            if (!Launcher.getDashboard().getChatUtil().privateMessagesEnabled()) {
                player.sendMessage(ChatColor.RED + "Private messages are currently disabled.");
                return;
            }
        }

        Mute mute = player.getMute();

        if (mute == null) {
            player.sendMessage(ChatColor.RED + "Please try chatting again in a moment. (Error Code 109)");
            return;
        }

        if (mute.isMuted()) {
            long releaseTime = mute.getRelease();
            Date currentTime = new Date();

            if (currentTime.getTime() > releaseTime) {
                Launcher.getDashboard().getSqlUtil().unmutePlayer(player.getUniqueId());
                player.getMute().setMuted(false);
            } else {
                String msg = ChatColor.RED + "You are silenced! You will be unsilenced in " +
                        DateUtil.formatDateDiff(mute.getRelease()) + ".";
                if (!mute.getReason().equals("")) {
                    msg += " Reason: " + player.getMute().getReason();
                }

                player.sendMessage(msg);
                return;
            }
        }

        String msg = "";
        for (String arg : args) {
            msg += arg + " ";
        }

        msg = player.getRank().getRankId() < Rank.SQUIRE.getRankId() ?
                Launcher.getDashboard().getChatUtil().removeCaps(player,
                msg.trim()) : msg.trim();

        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
            if (Launcher.getDashboard().getChatUtil().containsSwear(player, msg) ||
                    Launcher.getDashboard().getChatUtil().isAdvert(player, msg)
                    || Launcher.getDashboard().getChatUtil().spamCheck(player, msg) ||
                    Launcher.getDashboard().getChatUtil().containsUnicode(player, msg)) {
                return;
            }

            String mm = msg.toLowerCase().replace(".", "").replace("-", "").replace(",", "")
                    .replace("/", "").replace("_", "").replace(" ", "");
            if (mm.contains("skype") || mm.contains(" skyp ") || mm.startsWith("skyp ") || mm.endsWith(" skyp") || mm.contains("skyp*")) {
                player.sendMessage(ChatColor.RED + "Please do not ask for Skype information!");
                return;
            }
        }

        party.chat(player, msg);
        Launcher.getDashboard().getChatUtil().logMessage(player.getUniqueId(), "/pchat " + party.getLeader().getName() + " " + msg);
    }

    private boolean enoughTime(Player player) {
        return (((System.currentTimeMillis() - player.getLoginTime()) / 1000) + player.getOnlineTime()) >= 900;
    }
}