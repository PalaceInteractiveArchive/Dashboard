package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Marc on 10/3/16
 */
public class Commandkickall extends MagicCommand {

    public Commandkickall() {
        super(Rank.WIZARD);
    }

    @Override
    public void execute(final Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length <= 0) {
            player.sendMessage(ChatColor.RED + "/kickall [Message]");
            return;
        }
        StringBuilder r = new StringBuilder();
        for (String arg : args) {
            r.append(arg).append(" ");
        }
        r = new StringBuilder(ChatColor.translateAlternateColorCodes('&', r.toString().trim()));
        player.sendMessage(ChatColor.GREEN + "Disconnecting all players for " + r);
        for (Player tp : dashboard.getOnlinePlayers()) {
            if (tp.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                continue;
            }
            tp.kickPlayer(r.toString());
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                boolean empty = true;
                for (Player tp : dashboard.getOnlinePlayers()) {
                    if (tp.getRank().getRankId() < Rank.WIZARD.getRankId()) {
                        empty = false;
                        break;
                    }
                }
                if (empty) {
                    player.sendMessage(ChatColor.GREEN + "All players have been disconnected!");
                    cancel();
                }
            }
        }, 0, 1000);
    }
}