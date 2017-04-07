package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
                if (Dashboard.isTestNetwork()) {
                    return;
                }
                try (Connection connection = Dashboard.sqlUtil.getConnection()) {
                    PreparedStatement bans = connection.prepareStatement("UPDATE banned_players SET active=0 WHERE active=1 AND permanent=0 AND `release`<=NOW();");
                    int banCount = bans.executeUpdate();
                    bans.close();
//                    PreparedStatement bans = connection.prepareStatement("SELECT count(*) FROM banned_players WHERE active=1 AND permanent=0 AND `release`<=NOW();");
//                    ResultSet result1 = bans.executeQuery();
//                    int banCount = 0;
//                    if (result1.next()) {
//                        banCount = result1.getInt("count(*)");
//                    }
//                    result1.close();
                    bans.close();
                    if (banCount != 0) {
                        sendMessage(ChatColor.YELLOW + "" + banCount + ChatColor.GREEN +
                                (banCount == 1 ? " ban that expired was removed" : " bans that expired were removed"));
                    }

                    PreparedStatement mutes = connection.prepareStatement("UPDATE muted_players SET active=0 WHERE active=1 AND `release`<=NOW();");
                    int muteCount = mutes.executeUpdate();
                    mutes.close();
//                    PreparedStatement mutes = connection.prepareStatement("SELECT count(*) FROM muted_players WHERE active=1 AND `release`<=NOW();");
//                    ResultSet result2 = mutes.executeQuery();
//                    int muteCount = 0;
//                    if (result2.next()) {
//                        muteCount = result2.getInt("count(*)");
//                    }
//                    result2.close();
//                    mutes.close();
                    if (muteCount != 0) {
                        sendMessage(ChatColor.YELLOW + "" + muteCount + ChatColor.GREEN +
                                (muteCount == 1 ? " mute that expired was removed" : " mutes that expired were removed"));
                        for (Player tp : Dashboard.getOnlinePlayers()) {
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
        for (Player player : Dashboard.getOnlinePlayers()) {
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