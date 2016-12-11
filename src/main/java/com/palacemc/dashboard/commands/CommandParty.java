package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;

import java.util.Arrays;

public class CommandParty extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandParty() {
        aliases = Arrays.asList("p");
        tabCompletePlayers = true;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                helpMenu(player);
                return;
            }

            if (args[0].equalsIgnoreCase("accept")) {
                dashboard.getPartyUtil().acceptRequest(player);
                return;
            }

            if (args[0].equalsIgnoreCase("deny")) {
                dashboard.getPartyUtil().denyRequest(player);
                return;
            }

            Party party = dashboard.getPartyUtil().findPartyForPlayer(player);
            if (args[0].equalsIgnoreCase("close")) {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You're not in a Party!");
                    return;
                }

                if (!party.isLeader(player)) {
                    player.sendMessage(ChatColor.RED + "Only the Party Leader can use this!");
                    return;
                }
                party.close();
                return;
            }

            if (args[0].equalsIgnoreCase("leave")) {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You're not in a Party!");
                    return;
                }

                if (party.isLeader(player)) {
                    player.sendMessage(ChatColor.RED + "You cannot leave the Party, you're the Leader!")
                    ;
                    return;
                }

                party.leave(player);
                return;
            }
            if (args[0].equalsIgnoreCase("list")) {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You're not in a Party!");
                    return;
                }

                party.listMembersToMember(player);
                return;
            }
            if (args[0].equalsIgnoreCase("warp")) {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You're not in a Party!");
                    return;
                }

                if (!party.isLeader(player)) {
                    player.sendMessage(ChatColor.RED + "Only the Party Leader can use this!");
                    return;
                }

                party.warpToLeader();
                return;
            }
            if (party == null) {
                party = dashboard.getPartyUtil().createParty(player);
            }

            if (!party.isLeader(player)) {
                player.sendMessage(ChatColor.RED + "Only the Party Leader can invite players!");
                return;
            }

            Player tp = dashboard.getPlayer(args[0]);
            if (tp == null) {
                player.sendMessage(ChatColor.RED + "That player wasn't found!");
                return;
            }

            if (tp.getUuid().equals(player.getUuid())) {
                player.sendMessage(ChatColor.RED + "You cannot invite yourself!");
                return;
            }

            dashboard.getPartyUtil().invitePlayer(party, tp);
            return;
        } else if (args.length == 2) {
            Party party = dashboard.getPartyUtil().findPartyForPlayer(player);

            if (args[0].equalsIgnoreCase("takeover")) {
                if (player.getRank().getRankId() < Rank.KNIGHT.getRankId()) {
                    helpMenu(player);
                    return;
                }

                Player tp = dashboard.getPlayer(args[1]);
                if (tp == null) {
                    player.sendMessage("That player wasn't found!");
                    return;
                }

                if (party != null) {
                    if (!party.getMembers().contains(tp.getUuid())) {
                        player.sendMessage("You must first leave your current Party!")
                        ;
                        return;
                    }
                }

                party = dashboard.getPartyUtil().findPartyForPlayer(tp.getUuid());
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "This player is not in a Party!");
                    return;
                }

                if (party.isLeader(player)) {
                    player.sendMessage(ChatColor.RED + "You are already the Party Leader!");
                    return;
                }

                party.takeover(player);
                return;
            }

            if (party == null) {
                player.sendMessage("You're not in a Party!");
                return;
            }

            if (args[0].equalsIgnoreCase("remove")) {
                if (!party.isLeader(player)) {
                    player.sendMessage(ChatColor.RED + "Only the Party Leader can use this!");
                    return;
                }

                Player tp = dashboard.getPlayer(args[1]);

                if (tp == null) {
                    player.sendMessage(ChatColor.RED + "That player wasn't found!");
                    return;
                }
                party.remove(tp);
                return;
            }

            if (args[0].equalsIgnoreCase("promote")) {
                if (!party.isLeader(player)) {
                    player.sendMessage(ChatColor.RED + "Only the Party Leader can use this!");
                    return;
                }

                Player tp = dashboard.getPlayer(args[1]);
                if (tp == null) {
                    player.sendMessage("That player wasn't found!");
                    return;
                }

                if (!party.getMembers().contains(tp.getUuid())) {
                    player.sendMessage("That player isn't in your Party!");
                    return;
                }

                if (tp.getUuid().equals(player.getUuid())) {
                    player.sendMessage("You're already the Leader!");
                }

                party.promote(player, tp);
                return;
            }
        }
        helpMenu(player);
    }

    public void helpMenu(Player player) {
        String dash = ChatColor.GREEN + "- " + ChatColor.AQUA;
        String y = ChatColor.YELLOW.toString();
        player.sendMessage(y + "Party Commands:\n" + dash + "/party help " + y + "- Shows this help menu\n" + dash +
                "/party [player]" + y + "- Invite a player to your Party\n" + dash + "/party leave " + y +
                "- Leave your current Party\n" + dash + "/party list " + y + "- List all of the members in your Party\n"
                + dash + "/party promote [player] " + y + "- Promote a player to Party Leader\n" + dash +
                "/party accept " + y + "- Accept a Party invite from a player\n" + dash + "/party deny " + y +
                "- Deny a Party Request\n" + dash + "/party warp " + y +
                "- Brings the members of your Party to your server\n" + dash + "/party remove [player] " + y +
                "- Removes a player from your Party\n" + dash + "/pchat [message] " + y +
                "- Message members of your Party\n" + dash + "/party close " + y +
                "- Close your Party");
    }
}