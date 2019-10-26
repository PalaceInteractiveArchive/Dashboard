package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class ChatDelayCommand extends DashboardCommand {

    public ChatDelayCommand() {
        super(Rank.MOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "The chat delay is currently " +
                    (dashboard.getChatUtil().getChatDelay() / 1000) + " seconds!");
            player.sendMessage(ChatColor.GREEN + "Change delay: /chatdelay [Time]");
            return;
        }
        try {
            int time = Integer.parseInt(args[0]);
            dashboard.getChatUtil().setChatDelay(time * 1000);
            dashboard.getModerationUtil().changeChatDelay(time, player.getUsername());
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Please use a whole number :)");
        }
    }
}