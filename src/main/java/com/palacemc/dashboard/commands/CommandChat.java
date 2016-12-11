package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Marc on 9/25/16
 */
public class CommandChat extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    @Override
    public void execute(Player player, String label, String[] args) {
        List<String> list = new ArrayList<>();

        list.add("all");
        list.add("party");

        if (player.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
            list.add("staff");
            if (player.getRank().getRankId() >= Rank.EMPEROR.getRankId()) {
                list.add("admin");
            }
        }

        if (args.length <= 0) {
            String m = ChatColor.AQUA + "You are currently in the " + ChatColor.GREEN + player.getChannel() +
                    ChatColor.AQUA + " channel. You can speak in the following channels:";

            for (String s : list) {
                m += ChatColor.GREEN + "\n- " + ChatColor.AQUA + s;
            }

            m += "\n\nExample: " + ChatColor.GREEN + "/chat all " + ChatColor.AQUA + "switches you to main chat";

            player.sendMessage(m);
            return;
        }

        String channel = args[0].toLowerCase();

        if (!list.contains(channel)) {
            player.sendMessage(ChatColor.RED + "You can't join that channel, or it doesn't exist!");
            return;
        }

        if (channel.equals("party") && dashboard.getPartyUtil().findPartyForPlayer(player) == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a party! Invite a player with " + ChatColor.GREEN + "/party [Username]");
            return;
        }

        player.setChannel(channel);
        player.sendMessage(ChatColor.GREEN + "You have selected the " + ChatColor.AQUA + channel +
                ChatColor.GREEN + " channel");
    }

    @Override
    public Iterable<String> onTabComplete(Player player, List<String> args) {
        List<String> list = new ArrayList<>();

        if (args.size() == 0) {
            list.add("all");
            list.add("party");

            if (player.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                list.add("staff");

                if (player.getRank().getRankId() >= Rank.EMPEROR.getRankId()) {
                    list.add("admin");
                }
            }

            Collections.sort(list);
            return list;
        } else if (args.size() == 1) {
            list.add("all");
            list.add("party");

            if (player.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                list.add("staff");

                if (player.getRank().getRankId() >= Rank.EMPEROR.getRankId()) {
                    list.add("admin");
                }
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
        }
        return list;
    }
}