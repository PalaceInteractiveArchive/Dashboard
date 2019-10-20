package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Arrays;
import java.util.Collections;

public class ReplyCommand extends DashboardCommand {

    public ReplyCommand() {
        aliases = Collections.singletonList("r");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/reply [Message]");
            return;
        }
        Player tp = dashboard.getPlayer(player.getReply());
        if (player.getReply() == null || tp == null) {
            player.sendMessage(ChatColor.RED + "You don't have anyone to respond to!");
            return;
        }
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
            if (dashboard.getChatUtil().isMuted(player)) {
                return;
            }
            if (!tp.canRecieveMessages() || tp.isIgnored(player.getUniqueId()) && tp.getRank().getRankId() < Rank.CHARACTER.getRankId()) {
                player.sendMessage(ChatColor.RED + "This person has messages disabled!");
                return;
            }
            if (!dashboard.getChatUtil().privateMessagesEnabled()) {
                player.sendMessage(ChatColor.RED + "Private messages are currently disabled.");
                return;
            }
        }
        StringBuilder msg = new StringBuilder();
        for (String arg : args) {
            msg.append(arg).append(" ");
        }
        msg = new StringBuilder(player.getRank().getRankId() < Rank.TRAINEE.getRankId() ? dashboard.getChatUtil().removeCaps(player,
                msg.toString().trim()) : msg.toString().trim());
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
            if (dashboard.getChatUtil().containsSwear(player, msg.toString()) || dashboard.getChatUtil().isAdvert(player, msg.toString())
                    || dashboard.getChatUtil().spamCheck(player, msg.toString()) || dashboard.getChatUtil().containsUnicode(player, msg.toString())) {
                return;
            }
            String mm = msg.toString().toLowerCase().replace(".", "").replace("-", "").replace(",", "")
                    .replace("/", "").replace("_", "").replace(" ", "");
            if (mm.contains("skype") || mm.contains(" skyp ") || mm.startsWith("skyp ") || mm.endsWith(" skyp") || mm.contains("skyp*")) {
                player.sendMessage(ChatColor.RED + "Please do not ask for Skype information!");
                return;
            }
        }
        if (tp.hasMentions()) {
            tp.mention();
        }
        if (player.isIgnored(tp.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You ignore this player, they won't be able to respond!");
        }
        tp.sendMessage(player.getRank().getFormattedName() + ChatColor.GRAY + " " + player.getUsername() +
                ChatColor.GREEN + " -> " + ChatColor.LIGHT_PURPLE + "You: " + ChatColor.WHITE + msg);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You " + ChatColor.GREEN + "-> " +
                tp.getRank().getFormattedName() + ChatColor.GRAY + " " + tp.getUsername() + ": " +
                ChatColor.WHITE + msg);
        tp.setReply(player.getUniqueId());
        player.setReply(tp.getUniqueId());
        dashboard.getChatUtil().socialSpyMessage(player, tp, msg.toString(), "reply");
        dashboard.getChatUtil().logMessage(player.getUniqueId(), "/reply " + tp.getUsername() + " " + msg);
    }
}