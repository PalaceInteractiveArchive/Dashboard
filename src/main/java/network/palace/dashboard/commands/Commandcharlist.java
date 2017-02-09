package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.*;

public class Commandcharlist extends MagicCommand {

    public Commandcharlist() {
        super(Rank.CHARACTER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        HashMap<String, List<String>> servers = new HashMap<>();
        for (Player tp : Dashboard.getOnlinePlayers()) {
            if (tp == null || tp.getRank() == null || tp.getServer() == null) {
                continue;
            }
            if (tp.getRank().name().toLowerCase().contains("character")) {
                String server = tp.getServer();
                if (servers.containsKey(server)) {
                    servers.get(server).add(tp.getName());
                } else {
                    servers.put(server, Arrays.asList(tp.getName()));
                }
            }
        }
        if (servers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No Characters are online right now!");
        } else {
            List<String> msgs = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : servers.entrySet()) {
                String msg = ChatColor.GREEN + entry.getKey() + ": " + ChatColor.BLUE;
                List<String> list = new ArrayList<>(entry.getValue());
                for (int i = 0; i < list.size(); i++) {
                    String tp = list.get(i);
                    msg += tp;
                    if (i < (list.size() - 1)) {
                        msg += ",";
                    }
                }
                msgs.add(msg);
            }
            player.sendMessage(ChatColor.BLUE + "Online Characters:");
            for (String s : msgs) {
                player.sendMessage(s);
            }
        }
    }
}