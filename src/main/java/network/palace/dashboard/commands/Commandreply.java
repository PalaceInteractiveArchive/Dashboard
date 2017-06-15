package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Arrays;

public class Commandreply extends MagicCommand {

    public Commandreply() {
        aliases = Arrays.asList("r");
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
        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
            if (dashboard.getChatUtil().isMuted(player)) {
                return;
            }
            if (!tp.canRecieveMessages()) {
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
            if (dashboard.getChatUtil().containsSwear(player, msg) || dashboard.getChatUtil().isAdvert(player, msg)
                    || dashboard.getChatUtil().spamCheck(player, msg) || dashboard.getChatUtil().containsUnicode(player, msg)) {
                return;
            }
            String mm = msg.toLowerCase().replace(".", "").replace("-", "").replace(",", "")
                    .replace("/", "").replace("_", "").replace(" ", "");
            if (mm.contains("skype") || mm.contains(" skyp ") || mm.startsWith("skyp ") || mm.endsWith(" skyp") || mm.contains("skyp*")) {
                player.sendMessage(ChatColor.RED + "Please do not ask for Skype information!");
                return;
            }
        }
        if (tp.hasMentions()) {
            tp.mention();
        }
        tp.sendMessage(player.getRank().getFormattedName() + ChatColor.GRAY + " " + player.getUsername() +
                ChatColor.GREEN + " -> " + ChatColor.LIGHT_PURPLE + "You: " + ChatColor.WHITE + msg);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You " + ChatColor.GREEN + "-> " +
                tp.getRank().getFormattedName() + ChatColor.GRAY + " " + tp.getUsername() + ": " +
                ChatColor.WHITE + msg);
        tp.setReply(player.getUniqueId());
        dashboard.getChatUtil().socialSpyMessage(player, tp, msg, "reply");
        dashboard.getChatUtil().logMessage(player.getUniqueId(), "/reply " + tp.getUsername() + " " + msg);
    }
}