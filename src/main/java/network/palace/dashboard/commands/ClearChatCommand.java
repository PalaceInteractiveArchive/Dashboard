package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.HashMap;

public class ClearChatCommand extends DashboardCommand {
    public String clearMessage = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
    private HashMap<String, Long> lastCleared = new HashMap<>();

    public ClearChatCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        String server = player.getServer();
        boolean park = dashboard.getServer(server).isPark();
        if (System.currentTimeMillis() - (lastCleared.getOrDefault(park ? "ParkChat" : server, 0L)) < 2000) {
            //if this chat was last cleared up to 2 seconds ago, prevent it from being cleared again
            player.sendMessage(ChatColor.YELLOW + "It hasn't been 2 seconds since the last chat clear!");
            return;
        }
        lastCleared.put(park ? "ParkChat" : server, System.currentTimeMillis());
        HashMap<String, Boolean> parkChatCache = new HashMap<>();
        for (Player tp : dashboard.getOnlinePlayers()) {
            boolean clear;
            if (park) {
                if (parkChatCache.containsKey(tp.getServer())) {
                    clear = parkChatCache.get(tp.getServer());
                } else {
                    clear = dashboard.getServer(tp.getServer()).isPark();
                    parkChatCache.put(tp.getServer(), clear);
                }
            } else {
                clear = tp.getServer().equals(server);
            }
            if (clear) {
                if (tp.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
                    tp.sendMessage(clearMessage + ChatColor.DARK_AQUA + "Chat has been cleared");
                } else {
                    tp.sendMessage("\n" + ChatColor.DARK_AQUA + "Chat has been cleared by " + player.getUsername());
                }
            }
        }
    }
}