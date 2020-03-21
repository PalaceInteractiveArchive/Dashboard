package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.*;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JoinCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        List<String> servers = dashboard.getJoinServers();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && player.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                dashboard.loadJoinServers();
                player.sendMessage(ChatColor.GREEN + "Join Servers have been reloaded!");
                return;
            }
            if (exists(args[0])) {
                if (dashboard.getServer(player.getServer()).getServerType().equalsIgnoreCase(args[0])) {
                    player.sendMessage(ChatColor.RED + "You are already on this server!");
                    return;
                }
                try {
                    dashboard.getServerUtil().sendPlayerByType(player, formatName(args[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
            Server server;
            if (endsInNumber(args[0]) && exists(args[0].substring(0, args[0].length() - 1)) &&
                    (server = dashboard.getServerUtil().getServer(formatName(args[0]))) != null) {
                try {
                    dashboard.getServerUtil().sendPlayer(player, server);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
        }
        TextComponent top = new TextComponent(ChatColor.GREEN + "Here is a list of servers you can join: " +
                ChatColor.GRAY + "(Click to join)");
        player.sendMessage(top);
        for (String server : servers) {
            if (server.trim().isEmpty()) continue;
            TextComponent txt = new TextComponent(ChatColor.GREEN + "- " + ChatColor.AQUA + server);
            txt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(ChatColor.GREEN + "Click to join the " + ChatColor.AQUA +
                            server + ChatColor.GREEN + " server!").create()));
            txt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + server));
            player.sendMessage(txt);
        }
    }

    private boolean endsInNumber(String s) {
        try {
            Integer.parseInt(s.substring(s.length() - 1));
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private boolean exists(String s) {
        for (String server : Launcher.getDashboard().getJoinServers()) {
            if (server.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    private String formatName(String s) {
        StringBuilder ns = new StringBuilder();
        String t = s.replaceAll("\\d", "");
        if (t.length() < 4 && !t.equalsIgnoreCase("hub")) {
            for (char c : s.toCharArray()) {
                ns.append(Character.toUpperCase(c));
            }
            return ns.toString();
        }
        Character last = null;
        for (char c : s.toCharArray()) {
            if (last == null) {
                last = c;
                ns.append(Character.toUpperCase(c));
                continue;
            }
            if (Character.toString(last).equals(" ")) {
                ns.append(Character.toUpperCase(c));
            } else {
                ns.append(c);
            }
            last = c;
        }
        return ns.toString();
    }

    @Override
    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> list = new ArrayList<>(Launcher.getDashboard().getJoinServers());
        Collections.sort(list);
        if (args.size() == 0) {
            return list;
        }
        List<String> l2 = new ArrayList<>();
        String arg = args.get(args.size() - 1);
        for (String s : list) {
            if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                l2.add(s);
            }
        }
        Collections.sort(l2);
        return l2;
    }

    @Override
    public boolean doesTabComplete() {
        return true;
    }
}