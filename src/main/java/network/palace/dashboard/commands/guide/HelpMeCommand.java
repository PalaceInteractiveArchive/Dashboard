package network.palace.dashboard.commands.guide;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.DashboardConstants;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.utils.ChatUtil;

import java.util.Collections;

public class HelpMeCommand extends DashboardCommand {

    public HelpMeCommand() {
        aliases = Collections.singletonList("help");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (ChatUtil.notEnoughTime(player)) {
            player.sendMessage(DashboardConstants.NEW_GUEST);
            return;
        }
        if (args.length < 1 || label.equalsIgnoreCase("help")) {
            player.sendMessage(ChatColor.AQUA + "To get help, explain what you need help with:");
            player.sendMessage(ChatColor.AQUA + "/helpme [Reason]");
            return;
        }
        Dashboard dashboard = Launcher.getDashboard();
        player.sendMessage(ChatColor.GREEN + "Processing your help request...");
        if (!dashboard.getGuideUtil().canSubmitHelpRequest(player)) {
            player.sendMessage(ChatColor.RED + "You need to wait a little bit before sending another help request.");
            return;
        }
        if (dashboard.getGuideUtil().overloaded()) {
            player.sendMessage(ChatColor.AQUA + "We're currently receiving a high volume of help requests. We apologize for the inconvenience.");
            return;
        }
        StringBuilder request = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            request.append(args[i]);
            if (i <= (args.length - 1)) {
                request.append(" ");
            }
        }
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
            if (dashboard.getChatUtil().containsSwear(player, request.toString()) || dashboard.getChatUtil().isAdvert(player, request.toString())
                    || dashboard.getChatUtil().spamCheck(player, request.toString()) || dashboard.getChatUtil().containsUnicode(player, request.toString())) {
                return;
            }
            if (dashboard.getChatUtil().strictModeCheck(request.toString())) {
                player.sendMessage(ChatColor.RED + "Your message was similar to another recently said in chat and was marked as spam. We apologize if this was done in error, we're constantly improving our chat filter.");
                dashboard.getModerationUtil().announceSpamMessage(player.getUsername(), request.toString());
                dashboard.getLogger().info("CANCELLED CHAT EVENT STRICT MODE");
                return;
            }
        }
        dashboard.getGuideUtil().sendHelpRequest(player, request.toString());
    }
}
