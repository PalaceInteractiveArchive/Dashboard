package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.DashboardConstants;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.utils.ChatUtil;

import java.util.Arrays;

public class MsgCommand extends DashboardCommand {

    public MsgCommand() {
        tabCompletePlayers = true;
        aliases = Arrays.asList("m", "tell", "w");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/msg [Player] [Message]");
            return;
        }
        if (ChatUtil.notEnoughTime(player)) {
            player.sendMessage(DashboardConstants.NEW_GUEST);
            return;
        }
        String target = args[0];
        Player tp = dashboard.getPlayer(args[0]);
        if (tp == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId() && dashboard.getChatUtil().isMuted(player)) return;
        if (player.getRank().getRankId() < Rank.CHARACTER.getRankId()) {
            if (!tp.canRecieveMessages() || (tp.isIgnored(player.getUniqueId()) && tp.getRank().getRankId() < Rank.CHARACTER.getRankId())) {
                player.sendMessage(ChatColor.RED + "This person has messages disabled!");
                return;
            }
            if (!dashboard.getChatUtil().privateMessagesEnabled()) {
                player.sendMessage(ChatColor.RED + "Private messages are currently disabled.");
                return;
            }
        }
        StringBuilder msg = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            msg.append(args[i]).append(" ");
        }
        msg = new StringBuilder(player.getRank().getRankId() < Rank.TRAINEE.getRankId() ? dashboard.getChatUtil().removeCaps(player,
                msg.toString().trim()) : msg.toString().trim());
        if (player.getRank().getRankId() < Rank.TRAINEE.getRankId()) {
            if (dashboard.getChatUtil().containsSwear(player, msg.toString()) || dashboard.getChatUtil().isAdvert(player, msg.toString())
                    || dashboard.getChatUtil().spamCheck(player, msg.toString()) || dashboard.getChatUtil().containsUnicode(player, msg.toString())) {
                return;
            }
            if (dashboard.getChatUtil().strictModeCheck(msg.toString())) {
                player.sendMessage(ChatColor.RED + "Your message was similar to another recently said in chat and was marked as spam. We apologize if this was done in error, we're constantly improving our chat filter.");
                dashboard.getModerationUtil().announceSpamMessage(player.getUsername(), msg.toString());
                dashboard.getLogger().info("CANCELLED CHAT EVENT STRICT MODE");
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
        dashboard.getChatUtil().socialSpyMessage(player, tp, msg.toString(), "msg");
        dashboard.getChatUtil().logMessage(player.getUniqueId(), "/msg " + tp.getUsername() + " " + msg);
    }
}