package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketJoinCommand;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandJoin extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        List<String> servers = Dashboard.getJoinServers();
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && player.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                Dashboard.loadJoinServers();
                player.sendMessage(ChatColor.GREEN + "Join Servers have been reloaded!");
                return;
            }
            if (exists(args[0])) {
                if (Dashboard.getServer(player.getServer()).getServerType().equalsIgnoreCase(args[0])) {
                    player.sendMessage(ChatColor.RED + "You are already on this server!");
                    return;
                }
                try {
                    Dashboard.serverUtil.sendPlayerByType(player, formatName(args[0]));
                } catch (Exception ignored) {
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
            if (endsInNumber(args[0]) && exists(args[0].substring(0, args[0].length() - 1)) &&
                    Dashboard.serverUtil.getServer(formatName(args[0])) != null) {
                try {
                    Dashboard.serverUtil.sendPlayer(player, formatName(args[0]));
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
        for (String server : Dashboard.getJoinServers()) {
            if (server.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    private String formatName(String s) {
        String ns = "";
        if (s.replaceAll("\\d", "").length() < 4) {
            for (char c : s.toCharArray()) {
                ns += Character.toString(Character.toUpperCase(c));
            }
            return ns;
        }
        Character last = null;
        for (char c : s.toCharArray()) {
            if (last == null) {
                last = c;
                ns += Character.toString(Character.toUpperCase(c));
                continue;
            }
            if (Character.toString(last).equals(" ")) {
                ns += Character.toString(Character.toUpperCase(c));
            } else {
                ns += Character.toString(c);
            }
            last = c;
        }
        return ns;
    }

    @Override
    public Iterable<String> onTabComplete(Player sender, List<String> args) {
        List<String> list = new ArrayList<>();
        for (String s : Dashboard.getJoinServers()) {
            list.add(s);
        }
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