package network.palace.dashboard.commands.chat;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.RankTag;

import java.util.List;

public class GuideChatCommand extends DashboardCommand {

    public GuideChatCommand() {
        super(Rank.TRAINEE, RankTag.GUIDE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/gc [Message]");
            return;
        }
        String message = String.join(" ", args);
        String response;
        Rank rank = player.getRank();
        List<RankTag> tags = player.getTags();

        response = ChatColor.WHITE + "[" + ChatColor.DARK_GREEN + "GUIDE" + ChatColor.WHITE + "] " + RankTag.format(tags) +
                rank.getFormattedName() + " " + ChatColor.GRAY + player.getUsername() + ": " + ChatColor.GRAY + message;
        dashboard.getChatUtil().guideChatMessage(response);
        dashboard.getChatUtil().logMessage(player.getUniqueId(), "/gc " + player.getUsername() + " " + message);
    }
}