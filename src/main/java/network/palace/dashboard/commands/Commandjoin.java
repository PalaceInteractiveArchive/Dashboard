package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketJoinCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commandjoin extends MagicCommand {

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
                } catch (Exception ignored) {
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
            if (endsInNumber(args[0]) && exists(args[0].substring(0, args[0].length() - 1)) &&
                    dashboard.getServerUtil().getServer(formatName(args[0])) != null) {
                try {
                    dashboard.getServerUtil().sendPlayer(player, formatName(args[0]));
                } catch (Exception ignored) {
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
        }
        PacketJoinCommand packet = new PacketJoinCommand(player.getUniqueId(), servers);
        player.send(packet);
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
                ns.append(Character.toString(Character.toUpperCase(c)));
            }
            return ns.toString();
        }
        Character last = null;
        for (char c : s.toCharArray()) {
            if (last == null) {
                last = c;
                ns.append(Character.toString(Character.toUpperCase(c)));
                continue;
            }
            if (Character.toString(last).equals(" ")) {
                ns.append(Character.toString(Character.toUpperCase(c)));
            } else {
                ns.append(Character.toString(c));
            }
            last = c;
        }
        return ns.toString();
    }

    @Override
    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> list = new ArrayList<>();
        list.addAll(Launcher.getDashboard().getJoinServers());
        Collections.sort(list);
        if (args.size() == 0) {
            return list;
        }
        List<String> l2 = new ArrayList<>();
        String arg = args.get(args.size() - 1);
        for (String s : list) {
            if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                l2.add(s.toLowerCase());
            }
        }
        Collections.sort(l2);
        return l2;
    }
}