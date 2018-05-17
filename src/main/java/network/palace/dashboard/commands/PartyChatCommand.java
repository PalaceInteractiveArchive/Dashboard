package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.DashboardConstants;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.utils.ChatUtil;
import network.palace.dashboard.utils.DateUtil;

import java.util.Date;

public class PartyChatCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/pchat [Message]");
            return;
        }
        Party party = dashboard.getPartyUtil().findPartyForPlayer(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
            return;
        }
        if (!ChatUtil.enoughTime(player)) {
            player.sendMessage(DashboardConstants.NEW_GUEST);
            return;
        }
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
            if (dashboard.getChatUtil().isMuted(player)) {
                return;
            }
            if (!dashboard.getChatUtil().privateMessagesEnabled()) {
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
            long releaseTime = mute.getExpires();
            Date currentTime = new Date();
            if (currentTime.getTime() > releaseTime) {
                dashboard.getMongoHandler().unmutePlayer(player.getUniqueId());
                player.getMute().setMuted(false);
            } else {
                String msg = ChatColor.RED + "You are silenced! You will be unsilenced in " +
                        DateUtil.formatDateDiff(mute.getExpires()) + ".";
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
        msg = new StringBuilder(player.getRank().getRankId() < Rank.TRAINEE.getRankId() ? dashboard.getChatUtil().removeCaps(player,
                msg.toString().trim()) : msg.toString().trim());
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
            if (dashboard.getChatUtil().containsSwear(player, msg.toString()) || dashboard.getChatUtil().isAdvert(player, msg.toString())
                    || dashboard.getChatUtil().spamCheck(player, msg.toString()) || dashboard.getChatUtil().containsUnicode(player, msg.toString())) {
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
        dashboard.getChatUtil().logMessage(player.getUniqueId(), "/pchat " + party.getLeader().getUsername() + " " + msg);
    }
}