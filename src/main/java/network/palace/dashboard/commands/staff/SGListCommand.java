package network.palace.dashboard.commands.staff;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.*;

public class SGListCommand extends DashboardCommand {

    public SGListCommand() {
        super(Rank.SPECIALGUEST);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        HashMap<String, List<String>> servers = new HashMap<>();
        for (Player tp : dashboard.getOnlinePlayers()) {
            if (tp == null || tp.getRank() == null || tp.getServer() == null) continue;
            if (tp.getRank().equals(Rank.SPECIALGUEST)) {
                String server = tp.getServer();
                if (servers.containsKey(server)) {
                    List<String> characters = servers.remove(server);
                    characters.add(tp.getUsername());
                    servers.put(server, characters);
                } else {
                    List<String> list = new ArrayList<>(Collections.singletonList(tp.getUsername()));
                    servers.put(server, list);
                }
            }
        }
        if (servers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No Special Guests are online right now!");
        } else {
            List<String> msgs = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : servers.entrySet()) {
                StringBuilder msg = new StringBuilder(ChatColor.GREEN + entry.getKey() + ": " + ChatColor.DARK_PURPLE);
                List<String> list = new ArrayList<>(entry.getValue());
                for (int i = 0; i < list.size(); i++) {
                    String tp = list.get(i);
                    msg.append(tp);
                    if (i < (list.size() - 1)) {
                        msg.append(",");
                    }
                }
                msgs.add(msg.toString());
            }
            player.sendMessage(ChatColor.DARK_PURPLE + "Online Special Guests:");
            for (String s : msgs) {
                player.sendMessage(s);
            }
        }
    }
}