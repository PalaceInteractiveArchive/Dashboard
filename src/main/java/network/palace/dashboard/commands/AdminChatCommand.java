package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Rank;

public class AdminChatCommand extends DashboardCommand {

    public AdminChatCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length > 0) {
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            Dashboard dashboard = Launcher.getDashboard();
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() >= Rank.DEVELOPER.getRankId() && !tp.isDisabled()) {
                    tp.sendMessage(ChatColor.RED + "[ADMIN CHAT] " + ChatColor.GRAY + player.getUsername() + ": " +
                            ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message.toString()));
                }
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "/ho [Message]");
    }
}