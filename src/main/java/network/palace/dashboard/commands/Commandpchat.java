package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.utils.DateUtil;
import network.palace.dashboard.handlers.*;

import java.util.Date;

public class Commandpchat extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/pchat [Message]");
            return;
        }
        Party party = Dashboard.partyUtil.findPartyForPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }
        if (!enoughTime(player)) {
            player.sendMessage(ChatColor.RED + "New Guests must be on the server for at least 15 minutes before talking in chat. " +
                    ChatColor.DARK_AQUA + "Learn more at palnet.us/rules");
            return;
        }
        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
            if (Dashboard.chatUtil.isMuted(player)) {
                return;
            }
            if (!Dashboard.chatUtil.privateMessagesEnabled()) {
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
                Dashboard.sqlUtil.unmutePlayer(player.getUniqueId());
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
        StringBuilder msg = new StringBuilder();
        for (String arg : args) {
            msg.append(arg).append(" ");
        }
        msg = new StringBuilder(player.getRank().getRankId() < Rank.SQUIRE.getRankId() ? Dashboard.chatUtil.removeCaps(player,
                msg.toString().trim()) : msg.toString().trim());
        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
            if (Dashboard.chatUtil.containsSwear(player, msg.toString()) || Dashboard.chatUtil.isAdvert(player, msg.toString())
                    || Dashboard.chatUtil.spamCheck(player, msg.toString()) || Dashboard.chatUtil.containsUnicode(player, msg.toString())) {
                return;
            }
            String mm = msg.toString().toLowerCase().replace(".", "").replace("-", "").replace(",", "")
                    .replace("/", "").replace("_", "").replace(" ", "");
            if (mm.contains("skype") || mm.contains(" skyp ") || mm.startsWith("skyp ") || mm.endsWith(" skyp") || mm.contains("skyp*")) {
                player.sendMessage(ChatColor.RED + "Please do not ask for Skype information!");
                return;
            }
        }
        party.chat(player, msg.toString());
        Dashboard.chatUtil.logMessage(player.getUniqueId(), "/pchat " + party.getLeader().getName() + " " + msg);
    }

    private boolean enoughTime(Player player) {
        return (((System.currentTimeMillis() - player.getLoginTime()) / 1000) + player.getOnlineTime()) >= 900;
    }
}