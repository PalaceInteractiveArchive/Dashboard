package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class Commandsc extends MagicCommand {

    public Commandsc() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length > 0) {
            String message = String.join(" ", args);
            String response;
            Rank rank = player.getRank();

            response = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " + rank.getFormattedName()
                    + " " + ChatColor.GRAY + player.getUsername() + ": " + ChatColor.WHITE +
                    ChatColor.translateAlternateColorCodes('&', message);
            dashboard.getChatUtil().staffChatMessage(response);
            dashboard.getChatUtil().logMessage(player.getUniqueId(), "/sc " + player.getUsername() + " " + message);
            return;
        }
        player.sendMessage(ChatColor.RED + "/sc [Message]");
    }
}