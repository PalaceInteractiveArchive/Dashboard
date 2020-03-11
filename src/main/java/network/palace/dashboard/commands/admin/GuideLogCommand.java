package network.palace.dashboard.commands.admin;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.UUID;

public class GuideLogCommand extends DashboardCommand {

    public GuideLogCommand() {
        super(Rank.ADMIN);
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/guidelog [Username]");
            return;
        }
        String username = args[0];
        Player tp = dashboard.getPlayer(username);
        UUID uuid;
        if (tp == null) {
            uuid = dashboard.getMongoHandler().usernameToUUID(username);
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }
        } else {
            uuid = tp.getUniqueId();
            username = tp.getUsername();
        }
        String[] stats = dashboard.getMongoHandler().getHelpActivity(uuid).split(",");
        player.sendMessage(ChatColor.GREEN + "Guide Log for " + username + ": \n" + ChatColor.YELLOW +
                "Last Day: " + stats[0] + " requests\n" +
                "Last Week: " + stats[1] + " requests\n" +
                "Last Month: " + stats[2] + " requests\n" +
                "All Time: " + stats[3] + " requests");
    }
}
