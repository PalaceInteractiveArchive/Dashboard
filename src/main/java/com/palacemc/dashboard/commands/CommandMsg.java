package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;

import java.util.Arrays;

public class CommandMsg extends MagicCommand {

    private Dashboard dashboard = Launcher.getDashboard();

    public CommandMsg() {
        tabCompletePlayers = true;
        aliases = Arrays.asList("m", "whisper", "tell", "w", "t");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/msg [Player] [Message]");
            return;
        }

        if (!enoughTime(player)) {
            player.sendMessage(ChatColor.RED + "New Guests must be on the server for at least 15 minutes before talking in chat. " +
                    ChatColor.DARK_AQUA + "Learn more at mcmagic.us/rules#chat");
            return;
        }

        String target = args[0];
        Player tp = dashboard.getPlayer(args[0]);

        if (tp == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
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

        for (int i = 1; i < args.length; i++) {
            msg += args[i] + " ";
        }

        msg = player.getRank().getRankId() < Rank.SQUIRE.getRankId() ? dashboard.getChatUtil().removeCaps(player,
                msg.trim()) : msg.trim();

        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
            if (dashboard.getChatUtil().containsSwear(player, msg) ||
                    dashboard.getChatUtil().isAdvert(player, msg) ||
                    dashboard.getChatUtil().spamCheck(player, msg) ||
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
        player.setReply(tp.getUuid());

        dashboard.getChatUtil().socialSpyMessage(player, tp, msg, "msg");
        dashboard.getChatUtil().logMessage(player.getUuid(), "/msg " + tp.getUsername() + " " + msg);
    }

    private boolean enoughTime(Player player) {
        return (((System.currentTimeMillis() - player.getLoginTime()) / 1000) + player.getOnlineTime()) >= 900;
    }
}