package network.palace.dashboard.utils;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.conversions.Bson;

import java.util.*;

/**
 * Created by Marc on 8/20/16
 */
public class ModerationUtil {

    public ModerationUtil() {
        Bson banUpdate = Updates.set("bans.$.active", new BsonBoolean(false));

        Bson muteUpdate = Updates.set("mutes.$.active", new BsonBoolean(false));

        Dashboard dashboard = Launcher.getDashboard();
        if (dashboard.isTestNetwork()) {
            return;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Bson banFilter = Filters.elemMatch("bans", new BsonDocument("active", new BsonBoolean(true))
                        .append("permanent", new BsonBoolean(false))
                        .append("expires", new BsonDocument("$lt", new BsonInt64(System.currentTimeMillis()))));

                Bson muteFilter = Filters.elemMatch("mutes", new BsonDocument("active", new BsonBoolean(true))
                        .append("expires", new BsonDocument("$lt", new BsonInt64(System.currentTimeMillis()))));

                try {
                    UpdateResult banResult = dashboard.getMongoHandler().getPlayerCollection().updateMany(banFilter, banUpdate);
                    long banCount = banResult.getModifiedCount();

                    UpdateResult muteResult = dashboard.getMongoHandler().getPlayerCollection().updateMany(muteFilter, muteUpdate);
                    long muteCount = muteResult.getModifiedCount();

                    if (banCount != 0) {
                        sendMessage(ChatColor.YELLOW + "" + banCount + ChatColor.GREEN +
                                (banCount == 1 ? " ban that expired was removed" : " bans that expired were removed"));
                    }

                    if (muteCount != 0) {
                        sendMessage(ChatColor.YELLOW + "" + muteCount + ChatColor.GREEN +
                                (muteCount == 1 ? " mute that expired was removed" : " mutes that expired were removed"));
                        for (Player tp : dashboard.getOnlinePlayers()) {
                            Mute m = tp.getMute();
                            if (m.isMuted() && m.getExpires() <= System.currentTimeMillis()) {
                                m.setMuted(false);
                            }
                        }
                    }
                } catch (Exception e) {
                    Launcher.getDashboard().getLogger().error("Error removing expired punishments", e);
                }
            }
        }, 10 * 1000, 600 * 1000L);
    }

    public void announceBan(Ban ban) {
        sendMessage(ChatColor.GREEN + ban.getName() + ChatColor.RED + " was banned by " + ChatColor.GREEN +
                verifySource(ban.getSource()) + ChatColor.RED + " Reason: " + ChatColor.GREEN + ban.getReason() +
                ChatColor.RED + " Expires: " + ChatColor.GREEN + (ban.isPermanent() ? "Permanent" :
                DateUtil.formatDateDiff(ban.getExpires())));
    }

    public void announceBan(AddressBan ban) {
        sendRestrictedMessage(
                ChatColor.WHITE + "[" + ChatColor.RED + "Dashboard" + ChatColor.WHITE + "] " + ChatColor.GREEN +
                        "IP " + ban.getAddress() + ChatColor.RED + " was banned by " + ChatColor.GREEN + verifySource(ban.getSource())
                        + ChatColor.RED + " Reason: " + ChatColor.GREEN + ban.getReason(),
                ChatColor.WHITE + "[" + ChatColor.RED + "Dashboard" + ChatColor.WHITE + "] " + ChatColor.GREEN +
                        "IP X.X.X.X" + ChatColor.RED + " was banned by " + ChatColor.GREEN + verifySource(ban.getSource())
                        + ChatColor.RED + " Reason: " + ChatColor.GREEN + ban.getReason(),
                Rank.LEAD
        );
    }

    public void announceUnban(AddressBan ban) {
        sendRestrictedMessage(
                ChatColor.WHITE + "[" + ChatColor.RED + "Dashboard" + ChatColor.WHITE + "] " + ChatColor.GREEN +
                        "IP " + ban.getAddress() + ChatColor.RED + " has been unbanned by " + ChatColor.GREEN + verifySource(ban.getSource()),
                ChatColor.WHITE + "[" + ChatColor.RED + "Dashboard" + ChatColor.WHITE + "] " + ChatColor.GREEN +
                        "IP X.X.X.X" + ChatColor.RED + " has been unbanned by " + ChatColor.GREEN + verifySource(ban.getSource()),
                Rank.LEAD
        );
    }

    public void announceBan(ProviderBan ban) {
        sendMessage(ChatColor.GREEN + "ISP " + ban.getProvider() + ChatColor.RED + " was banned by " + ChatColor.GREEN +
                verifySource(ban.getSource()));
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
                verifySource(mute.getSource()) + ChatColor.RED + " Reason: " + ChatColor.GREEN + mute.getReason() + ChatColor.RED +
                " Expires: " + ChatColor.GREEN + DateUtil.formatDateDiff(mute.getExpires()));
    }

    public void announceUnmute(String name, String source) {
        sendMessage(ChatColor.GREEN + name + ChatColor.RED + " has been unmuted by " + ChatColor.RED + source);
    }

    public void announceWarning(String name, String reason, String source) {
        sendMessage(ChatColor.GREEN + name + ChatColor.RED + " was issued a warning by " + ChatColor.GREEN + source +
                ChatColor.RED + " Reason: " + ChatColor.GREEN + reason);
    }

    public void changeChatDelay(int time, String source) {
        sendMessage(ChatColor.GREEN + "The chat delay was set to " + time + " seconds by " + source);
    }

    public void rankChange(String name, Rank rank, List<RankTag> tags, String source) {
        sendMessage(ChatColor.GREEN + name + "'s rank has been changed by " + source + " to " +
                RankTag.format(tags) + rank.getFormattedName());
    }

    public void sendMessage(String message) {
        Dashboard dashboard = Launcher.getDashboard();
        String msg = ChatColor.WHITE + "[" + ChatColor.RED + "Dashboard" + ChatColor.WHITE + "] " + message;
        for (Player player : dashboard.getOnlinePlayers()) {
            if (player.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                player.sendMessage(msg);
            }
        }
        dashboard.getLogger().warn(msg);
    }

    public void sendRestrictedMessage(String fullMessage, String blockedMessage, Rank cutoffRank) {
        Dashboard dashboard = Launcher.getDashboard();
        for (Player player : dashboard.getOnlinePlayers()) {
            int id = player.getRank().getRankId();
            if (id < Rank.TRAINEE.getRankId()) continue;
            if (id < cutoffRank.getRankId()) {
                player.sendMessage(blockedMessage);
            } else {
                player.sendMessage(fullMessage);
            }
        }
        dashboard.getLogger().warn(fullMessage);
    }

    public void togglePrivate(boolean enabled, String name) {
        sendMessage(ChatColor.GREEN + "Private messages have been " + (enabled ? "enabled" : ChatColor.RED +
                "disabled" + ChatColor.GREEN) + " by " + name);
    }

    public void displayServerMute(String name, boolean muted) {
        ChatColor prefix = muted ? ChatColor.RED : ChatColor.GREEN;
        String message = name + " will " + (muted ? "not" : "now") + " announce connects and disconnects to/from Dashboard.";
        sendMessage(prefix + message);
        SlackMessage slackMessage = new SlackMessage("");
        SlackAttachment attachment = new SlackAttachment(message);
        attachment.color(muted ? "danger" : "good");
        Launcher.getDashboard().getSlackUtil().sendDashboardMessage(slackMessage, Collections.singletonList(attachment));
    }

    public static String verifySource(String source) {
        Dashboard dashboard = Launcher.getDashboard();
        source = source.trim();
        if (source.length() == 36) {
            try {
                UUID sourceUUID = UUID.fromString(source);
                String name = dashboard.getCachedName(sourceUUID);
                if (name == null) {
                    name = dashboard.getMongoHandler().uuidToUsername(sourceUUID);
                    if (name == null) {
                        name = "Unknown";
                    } else {
                        dashboard.addToCache(sourceUUID, name);
                    }
                }
                source = name;
            } catch (Exception ignored) {
            }
        }
        return source;
    }

    public void announceSpamWhitelistAdd(SpamIPWhitelist whitelist) {
        sendRestrictedMessage(
                ChatColor.GREEN + whitelist.getAddress() + " is now whitelisted from Spam IP protection with a limit of " +
                        ChatColor.YELLOW + whitelist.getLimit() + ChatColor.GREEN + " accounts",
                ChatColor.GREEN + "X.X.X.X is now whitelisted from Spam IP protection with a limit of " +
                        ChatColor.YELLOW + whitelist.getLimit() + ChatColor.GREEN + " accounts",
                Rank.LEAD
        );
    }

    public void announceSpamWhitelistRemove(String ip) {
        sendRestrictedMessage(
                ChatColor.GREEN + ip + " is no longer whitelisted from Spam IP protection",
                ChatColor.GREEN + "X.X.X.X is no longer whitelisted from Spam IP protection",
                Rank.LEAD
        );
    }

    public void announceSpamIPConnect(int limit, String address) {
        sendRestrictedMessage(
                ChatColor.RED + "IP " + ChatColor.GREEN + address + ChatColor.RED +
                        " reached its maximum allowed player count of " + ChatColor.GREEN + limit + ChatColor.RED + " players",
                ChatColor.RED + "IP " + ChatColor.GREEN + "X.X.X.X" + ChatColor.RED +
                        " reached its maximum allowed player count of " + ChatColor.GREEN + limit + ChatColor.RED + " players",
                Rank.LEAD
        );
    }

    public void announceSpamMessage(String username, String message) {
        sendMessage(ChatColor.GREEN + username + "'s " + ChatColor.RED + "message " + ChatColor.AQUA + message + ChatColor.GREEN + " was marked as potential spam.");
    }
}