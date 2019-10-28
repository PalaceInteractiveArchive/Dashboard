package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ClickEvent;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.chat.HoverEvent;
import network.palace.dashboard.utils.DateUtil;
import network.palace.dashboard.utils.ModerationUtil;

import java.util.UUID;

public class BseenCommand extends DashboardCommand {

    public BseenCommand() {
        super(Rank.TRAINEE);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/bseen [Username]");
            return;
        }
        dashboard.getSchedulerManager().runAsync(() -> {
            try {
                Player tp = dashboard.getPlayer(args[0]);
                boolean online = tp != null;
                String name = online ? tp.getUsername() : args[0];
                UUID uuid;
                if (online) {
                    uuid = tp.getUniqueId();
                } else {
                    uuid = dashboard.getMongoHandler().usernameToUUID(args[0]);
                }
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "That player can't be found!");
                    return;
                }
                Rank rank;
                SponsorTier tier;
                long lastLogin;
                String ip;
                Mute mute;
                String server;
                if (online) {
                    rank = tp.getRank();
                    tier = tp.getSponsorTier();
                    lastLogin = tp.getLoginTime();
                    ip = tp.getAddress();
                    mute = tp.getMute();
                    server = tp.getServer();
                } else {
                    BseenData data = dashboard.getMongoHandler().getBseenInformation(uuid);
                    rank = data.getRank();
                    tier = data.getSponsorTier();
                    lastLogin = data.getLastLogin();
                    ip = data.getIpAddress();
                    server = data.getServer();
                    Ban ban = dashboard.getMongoHandler().getCurrentBan(uuid, name);
                    if (ban != null) {
                        String type = ban.isPermanent() ? "Permanently" : ("Temporarily (Expires: " +
                                DateUtil.formatDateDiff(ban.getExpires()) + ")");
                        player.sendMessage(ChatColor.RED + name + " is Banned " + type + " for " + ban.getReason() +
                                " by " + ModerationUtil.verifySource(ban.getSource()));
                    }
                    mute = dashboard.getMongoHandler().getCurrentMute(uuid, name);
                }
                if (mute != null && mute.isMuted()) {
                    player.sendMessage(ChatColor.RED + name + " is Muted for " +
                            DateUtil.formatDateDiff(mute.getExpires()) + " by " + ModerationUtil.verifySource(mute.getSource()) +
                            ". Reason: " + mute.getReason());
                }
                player.sendMessage(ChatColor.GREEN + name + " has been " + (online ? "online" : "away") + " for " +
                        DateUtil.formatDateDiff(lastLogin));
                player.sendMessage(ChatColor.RED + "Rank: " + rank.getFormattedName());
                if (!tier.equals(SponsorTier.NONE))
                    player.sendMessage(ChatColor.RED + "Sponsor: " + tier.getColor() + tier.getName());

                String divider = " - ";
                player.sendMessage(new ComponentBuilder(ip).color(ChatColor.AQUA)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ipseen " + ip))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Click to run an IP Search").color(ChatColor.AQUA)
                                        .create())).append(divider).color(ChatColor.DARK_GREEN)
                        .append("Name Check").color(ChatColor.LIGHT_PURPLE)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/namecheck " + name))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Click to run a Name Check").color(ChatColor.AQUA)
                                        .create())).append(divider).color(ChatColor.DARK_GREEN)
                        .append("Mod Log").color(ChatColor.GREEN)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Review Moderation History").color(ChatColor.GREEN)
                                        .create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/modlog " + name)).append("\n" + (online ? "Current" : "Last") +
                                " Server: ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW)
                        .append(server).color(ChatColor.AQUA).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Click to join this server!").color(ChatColor.GREEN)
                                        .create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/server " + server)).create());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}