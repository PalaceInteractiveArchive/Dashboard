package network.palace.dashboard.commands.admin;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SendCommand extends DashboardCommand {

    public SendCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "/send [player|all|current] [Target]");
            return;
        }
        Server server = dashboard.getServerUtil().getServer(args[1]);
        if (server == null) {
            player.sendMessage(ChatColor.RED + "The server '" + args[1] + "' does not exist!");
            return;
        }
        switch (args[0]) {
            case "all": {
                player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + "all players" + ChatColor.GREEN +
                        " to " + ChatColor.YELLOW + server.getName());
                for (Player tp : dashboard.getOnlinePlayers()) {
                    dashboard.getServerUtil().sendPlayer(tp, server);
                }
                return;
            }
            case "current": {
                String name = player.getServer();
                player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + "players on " + name + " (" +
                        dashboard.getServer(name).getCount() + " players)" + ChatColor.GREEN + " to " +
                        ChatColor.YELLOW + server.getName());
                for (Player tp : dashboard.getOnlinePlayers()) {
                    if (tp == null)
                        continue;
                    if (tp.getServer().equals(name)) {
                        dashboard.getServerUtil().sendPlayer(tp, server);
                    }
                }
                return;
            }
            default: {
                Player tp = dashboard.getPlayer(args[0]);
                if (tp == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                player.sendMessage(ChatColor.GREEN + "Sending " + ChatColor.GOLD + tp.getUsername() + ChatColor.GREEN +
                        " to " + ChatColor.YELLOW + server.getName());
                dashboard.getServerUtil().sendPlayer(tp, server);
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(Player player, List<String> args) {
        Dashboard dashboard = Launcher.getDashboard();
        List<String> list = new ArrayList<>();
        if (args.size() == 0) {
            list.add("all");
            list.add("current");
            for (Player tp : dashboard.getOnlinePlayers()) {
                list.add(tp.getUsername());
            }
            Collections.sort(list);
            return list;
        } else if (args.size() == 1) {
            list.add("all");
            list.add("current");
            for (Player tp : dashboard.getOnlinePlayers()) {
                list.add(tp.getUsername());
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
            for (Server server : dashboard.getServers()) {
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

    @Override
    public boolean doesTabComplete() {
        return true;
    }
}