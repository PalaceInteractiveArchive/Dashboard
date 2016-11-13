package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.handlers.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commandsend extends MagicCommand {

    public Commandsend() {
        super(Rank.WIZARD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "/send [player|all|current] [Target]");
            return;
        }
        Server server = Dashboard.serverUtil.getServer(args[1]);
        if (server == null) {
            player.sendMessage(ChatColor.RED + "The server '" + args[1] + "' does not exist!");
            return;
        }
        switch (args[0]) {
            case "all": {
                player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + "all players" + ChatColor.GREEN +
                        " to " + ChatColor.YELLOW + server.getName());
                for (Player tp : Dashboard.getOnlinePlayers()) {
                    Dashboard.serverUtil.sendPlayer(tp, server.getName());
                }
                return;
            }
            case "current": {
                String name = player.getServer();
                player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + "players on " + name + " (" +
                        Dashboard.getServer(name).getCount() + " players)" + ChatColor.GREEN + " to " +
                        ChatColor.YELLOW + server.getName());
                for (Player tp : Dashboard.getOnlinePlayers()) {
                    if (tp.getServer().equals(name)) {
                        Dashboard.serverUtil.sendPlayer(tp, server.getName());
                    }
                }
                return;
            }
            default: {
                Player tp = Dashboard.getPlayer(args[0]);
                if (tp == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + tp.getName() + ChatColor.GREEN +
                        " to " + ChatColor.YELLOW + server.getName());
                Dashboard.serverUtil.sendPlayer(tp, server.getName());
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(Player player, List<String> args) {
        List<String> list = new ArrayList<>();
        if (args.size() == 0) {
            list.add("all");
            list.add("current");
            for (Player tp : Dashboard.getOnlinePlayers()) {
                list.add(tp.getName());
            }
            Collections.sort(list);
            return list;
        } else if (args.size() == 1) {
            list.add("all");
            list.add("current");
            for (Player tp : Dashboard.getOnlinePlayers()) {
                list.add(tp.getName());
            }
            String arg = args.get(0);
            List<String> l2 = new ArrayList<>();
            for (String s : list) {
                if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                    l2.add(s);
                }
            }
            Collections.sort(l2);
            return l2;
        } else if (args.size() == 2) {
            for (Server server : Dashboard.getServers()) {
                list.add(server.getName());
            }
            String arg = args.get(1);
            List<String> l2 = new ArrayList<>();
            for (String s : list) {
                if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                    l2.add(s);
                }
            }
            Collections.sort(l2);
            return l2;
        }
        return list;
    }
}