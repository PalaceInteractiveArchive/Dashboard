package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Rank;

public class CommandChatDelay extends MagicCommand {

    public CommandChatDelay() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "The chat delay is currently " +
                    (Dashboard.chatUtil.getChatDelay() / 1000) + " seconds!");
            player.sendMessage(ChatColor.GREEN + "Change delay: /chatdelay [Time]");
            return;
        }
        try {
            int time = Integer.parseInt(args[0]);
            Dashboard.chatUtil.setChatDelay(time * 1000);
            Dashboard.moderationUtil.changeChatDelay(time, player.getName());
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please use a whole number :)");
        }
    }
}