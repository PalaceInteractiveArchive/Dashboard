package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.Collections;

public class CommandReply extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandReply() {
        aliases = Collections.singletonList("r");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/reply [Message]");
            return;
        }

        Player tp = dashboard.getPlayer(player.getReply());
        if (player.getReply() == null || tp == null) {
            player.sendMessage(ChatColor.RED + "You don't have anyone to respond to!");
            return;
        }

        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
            if (dashboard.getChatUtil().isMuted(player)) {
                return;
            }

            if (!tp.isRecieveMessages()) {
                player.sendMessage(ChatColor.RED + "This person has messages disabled!");
                return;
            }
            if (!dashboard.getChatUtil().privateMessagesEnabled()) {
                player.sendMessage(ChatColor.RED + "Private messages are currently disabled.");
                return;
            }
        }

        String msg = "";

        for (String arg : args) {
            msg += arg + " ";
        }

        msg = player.getRank().getRankId() < Rank.SQUIRE.getRankId() ? dashboard.getChatUtil().removeCaps(player,
                msg.trim()) : msg.trim();

        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
            if (dashboard.getChatUtil().containsSwear(player, msg) ||
                    dashboard.getChatUtil().isAdvert(player, msg)
                    || dashboard.getChatUtil().spamCheck(player, msg) ||
                    dashboard.getChatUtil().containsUnicode(player, msg)) {
                return;
            }

            String mm = msg.toLowerCase().replace(".", "").replace("-", "").replace(",", "")
                    .replace("/", "").replace("_", "").replace(" ", "");
            if (mm.contains("skype") || mm.contains(" skyp ") || mm.startsWith("skyp ") || mm.endsWith(" skyp") || mm.contains("skyp*")) {
                player.sendMessage(ChatColor.RED + "Please do not ask for Skype information!");
                return;
            }
        }

        if (tp.isMentions()) {
            tp.mention();
        }

        tp.sendMessage(player.getRank().getNameWithBrackets() + ChatColor.GRAY + " " + player.getUsername() +
                ChatColor.GREEN + " -> " + ChatColor.LIGHT_PURPLE + "You: " + ChatColor.WHITE + msg);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You " + ChatColor.GREEN + "-> " +
                tp.getRank().getNameWithBrackets() + ChatColor.GRAY + " " + tp.getUsername() + ": " +
                ChatColor.WHITE + msg);
        tp.setReply(player.getUuid());

        dashboard.getChatUtil().socialSpyMessage(player, tp, msg, "reply");
        dashboard.getChatUtil().logMessage(player.getUuid(), "/reply " + tp.getUsername() + " " + msg);
    }
}