package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Marc on 8/20/16
 */
public class ModerationUtil {

    public ModerationUtil() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Dashboard dashboard = Launcher.getDashboard();
                if (dashboard.isTestNetwork()) {
                    return;
                }
                try (Connection connection = dashboard.getSqlUtil().getConnection()) {
                    PreparedStatement bans = connection.prepareStatement("UPDATE banned_players SET active=0 WHERE active=1 AND permanent=0 AND `release`<=NOW();");
                    int banCount = bans.executeUpdate();
                    bans.close();
                    bans.close();
                    if (banCount != 0) {
                        sendMessage(ChatColor.YELLOW + "" + banCount + ChatColor.GREEN +
                                (banCount == 1 ? " ban that expired was removed" : " bans that expired were removed"));
                    }

                    PreparedStatement mutes = connection.prepareStatement("UPDATE muted_players SET active=0 WHERE active=1 AND `release`<=NOW();");
                    int muteCount = mutes.executeUpdate();
                    mutes.close();
                    if (muteCount != 0) {
                        sendMessage(ChatColor.YELLOW + "" + muteCount + ChatColor.GREEN +
                                (muteCount == 1 ? " mute that expired was removed" : " mutes that expired were removed"));
                        for (Player tp : dashboard.getOnlinePlayers()) {
                            Mute m = tp.getMute();
                            if (m.isMuted() && m.getRelease() <= System.currentTimeMillis()) {
                                m.setMuted(false);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, 60000L, 600000L);
    }

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

    public void announceBan(ProviderBan ban) {
        sendMessage(ChatColor.GREEN + "ISP " + ban.getProvider() + ChatColor.RED + " was banned by " + ChatColor.GREEN +
                ban.getSource());
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
        sendMessage(ChatColor.GREEN + name + "'s rank has been changed to " + rank.getFormattedName() +
                ChatColor.GREEN + " by " + source);
    }

    public void sendMessage(String message) {
        Dashboard dashboard = Launcher.getDashboard();
        String msg = ChatColor.WHITE + "[" + ChatColor.RED + "Dashboard" + ChatColor.WHITE + "] " + message;
        for (Player player : dashboard.getOnlinePlayers()) {
            if (player.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                player.sendMessage(msg);
            }
        }
    }

    public void togglePrivate(boolean enabled, String name) {
        sendMessage(ChatColor.GREEN + "Private messages have been " + (enabled ? "enabled" : ChatColor.RED +
                "disabled" + ChatColor.GREEN) + " by " + name);
    }

    public void displayServerMute(String name, boolean muted) {
        ChatColor prefix = muted ? ChatColor.RED : ChatColor.GREEN;
        String message = muted ? name + " has been muted!" : name + " has been unmuted!";
        sendMessage(prefix + message);
        SlackMessage slackMessage = new SlackMessage("");
        SlackAttachment attachment = new SlackAttachment(message);
        attachment.color(muted ? "danger" : "good");
        Launcher.getDashboard().getSlackUtil().sendDashboardMessage(slackMessage, Collections.singletonList(attachment));
    }
}