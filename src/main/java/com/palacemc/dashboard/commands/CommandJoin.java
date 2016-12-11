package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.packets.dashboard.PacketJoinCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandJoin extends MagicCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        List<String> servers = Launcher.getDashboard().getJoinServers();

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload") && player.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                Launcher.getDashboard().loadJoinServers();
                player.sendMessage(ChatColor.GREEN + "Join Servers have been reloaded!");
                return;
            }

            if (exists(args[0])) {
                if (Launcher.getDashboard().getServer(player.getServer()).getServerType().equalsIgnoreCase(args[0])) {
                    player.sendMessage(ChatColor.RED + "You are already on this server!");
                    return;
                }

                try {
                    Launcher.getDashboard().getServerUtil().sendPlayerByType(player, formatName(args[0]));
                } catch (Exception ignored) {
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }

            if (endsInNumber(args[0]) && exists(args[0].substring(0, args[0].length() - 1)) &&
                    Launcher.getDashboard().getServerUtil().getServer(formatName(args[0])) != null) {
                try {
                    Launcher.getDashboard().getServerUtil().sendPlayer(player, formatName(args[0]));
                } catch (Exception ignored) {
                    player.sendMessage(ChatColor.RED + "There was a problem joining that server!");
                }
                return;
            }
        }
        PacketJoinCommand packet = new PacketJoinCommand(player.getUuid(), servers);
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
        for (String s : Launcher.getDashboard().getJoinServers()) {
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