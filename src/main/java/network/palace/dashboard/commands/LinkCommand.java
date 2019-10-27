package network.palace.dashboard.commands;

import network.palace.dashboard.Launcher;
import network.palace.dashboard.forums.Forum;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;

/**
 * Created by Marc on 12/12/16.
 */
public class LinkCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
//        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
//            player.sendMessage(ChatColor.YELLOW + "You will be able to link your forum account with your Minecraft account soon!");
//            return;
//        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/link [email address]");
            return;
        }
        Forum forum = Launcher.getDashboard().getForum();
        switch (args[0].toLowerCase()) {
            case "cancel": {
                forum.unlinkAccount(player);
                return;
            }
            case "confirm": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "/link confirm [six-digit code]");
                    return;
                }
                forum.confirm(player, args[1]);
                return;
            }
        }
        String email = args[0];
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            player.sendMessage(ChatColor.RED + "That isn't a valid email!");
            return;
        }
        Launcher.getDashboard().getForum().linkAccount(player, email);
    }
}
