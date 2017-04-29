package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Arrays;

public class Commandmsg extends MagicCommand {

    public Commandmsg() {
        tabCompletePlayers = true;
        aliases = Arrays.asList("m", "whisper", "tell", "w", "t");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/msg [Player] [Message]");
            return;
        }
        if (!enoughTime(player)) {
            player.sendMessage(ChatColor.RED + "New Guests must be on the server for at least 15 minutes before talking in chat. " +
                    ChatColor.DARK_AQUA + "Learn more at palnet.us/rules");
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
            if (!tp.canRecieveMessages()) {
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
        msg = new StringBuilder(player.getRank().getRankId() < Rank.SQUIRE.getRankId() ? dashboard.getChatUtil().removeCaps(player,
                msg.toString().trim()) : msg.toString().trim());
        if (player.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
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
        tp.sendMessage(player.getRank().getNameWithBrackets() + ChatColor.GRAY + " " + player.getName() +
                ChatColor.GREEN + " -> " + ChatColor.LIGHT_PURPLE + "You: " + ChatColor.WHITE + msg);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You " + ChatColor.GREEN + "-> " +
                tp.getRank().getNameWithBrackets() + ChatColor.GRAY + " " + tp.getName() + ": " +
                ChatColor.WHITE + msg);
        tp.setReply(player.getUniqueId());
        player.setReply(tp.getUniqueId());
        dashboard.getChatUtil().socialSpyMessage(player, tp, msg.toString(), "msg");
        dashboard.getChatUtil().logMessage(player.getUniqueId(), "/msg " + tp.getName() + " " + msg);
    }

    private boolean enoughTime(Player player) {
        return (((System.currentTimeMillis() - player.getLoginTime()) / 1000) + player.getOnlineTime()) >= 900;
    }
}