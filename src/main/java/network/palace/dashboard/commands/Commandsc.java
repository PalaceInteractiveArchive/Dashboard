package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Rank;

public class Commandsc extends MagicCommand {

    public Commandsc() {
        super(Rank.SQUIRE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length > 0) {
            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }
            String msg;
            player = Dashboard.getPlayer(player.getUniqueId());
            Rank rank = player.getRank();
            msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " + rank.getNameWithBrackets()
                    + " " + ChatColor.GRAY + player.getName() + ": " + ChatColor.WHITE +
                    ChatColor.translateAlternateColorCodes('&', message);
            Dashboard.chatUtil.staffChatMessage(msg);
            if (player != null) {
                Dashboard.chatUtil.logMessage(player.getUniqueId(), "/sc " + player.getName() + " " + message);
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "/sc [Message]");
    }
}