package network.palace.dashboard.commands.moderation;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.*;
import network.palace.dashboard.handlers.*;

import java.util.List;

public class IPSeenCommand extends DashboardCommand {

    public IPSeenCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(final Player player, String label, final String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/ipseen [IP Address]");
            return;
        }
        String ip = args[0];
        dashboard.getSchedulerManager().runAsync(() -> {
            AddressBan ban = dashboard.getMongoHandler().getAddressBan(ip);
            if (ban != null) {
                player.sendMessage(ChatColor.RED + "This IP Address is banned for " + ChatColor.AQUA + ban.getReason());
            }
            SpamIPWhitelist spamIPWhitelist = dashboard.getMongoHandler().getSpamIPWhitelist(ip);
            if (spamIPWhitelist != null) {
                player.sendMessage(ChatColor.GREEN + "This IP Address is whitelisted from Spam IP protection with a player limit of " +
                        spamIPWhitelist.getLimit() + " players.");
            }
            List<String> users = dashboard.getMongoHandler().getPlayersOnIP(ip);
            if (users == null || users.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No users found on that IP Address.");
                return;
            }
            if (users.size() > 30) {
                player.sendMessage(ChatColor.RED + "There are more than 30 players on that IP Address! If you need the list message Legobuilder0813 on Slack.");
                return;
            }
            ComponentBuilder ulist = new ComponentBuilder("");
            for (int i = 0; i < users.size(); i++) {
                String s = users.get(i);
                if (i == (users.size() - 1)) {
                    ulist.append(s).color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bseen "
                            + s)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to search this Player!").color(ChatColor.GREEN).create()));
                    continue;
                }
                ulist.append(s).color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bseen "
                        + s)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to search this Player!").color(ChatColor.GREEN).create())).append(", ");
            }
            BaseComponent[] msg = new ComponentBuilder("Users on the IP Address " + ip + ":").color(ChatColor.AQUA).create();
            player.sendMessage(msg);
            player.sendMessage(ulist.create());
        });
    }
}