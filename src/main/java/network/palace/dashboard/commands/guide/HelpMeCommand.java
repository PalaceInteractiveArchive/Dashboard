package network.palace.dashboard.commands.guide;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

import java.util.Collections;

public class HelpMeCommand extends DashboardCommand {

    public HelpMeCommand() {
        aliases = Collections.singletonList("help");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
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
        dashboard.getGuideUtil().sendHelpRequest(player, request.toString());
    }
}
