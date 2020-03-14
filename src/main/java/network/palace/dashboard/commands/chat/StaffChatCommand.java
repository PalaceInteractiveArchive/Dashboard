package network.palace.dashboard.commands.chat;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.RankTag;

import java.util.List;

public class StaffChatCommand extends DashboardCommand {

    public StaffChatCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/sc [Message]");
            return;
        }
        String message = String.join(" ", args);
        String response;
        Rank rank = player.getRank();
        List<RankTag> tags = player.getTags();

        response = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " + RankTag.format(tags) +
                rank.getFormattedName() + " " + ChatColor.GRAY + player.getUsername() + ": " + ChatColor.WHITE +
                ChatColor.translateAlternateColorCodes('&', message);
        dashboard.getChatUtil().staffChatMessage(response);
        dashboard.getChatUtil().logMessage(player.getUniqueId(), "/sc " + player.getUsername() + " " + message);
    }
}