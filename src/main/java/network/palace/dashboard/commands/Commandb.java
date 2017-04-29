package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

public class Commandb extends MagicCommand {

    public Commandb() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length > 0) {
            String message = "";
            for (String arg : args) {
                message += arg + " ";
            }
            String sname = player.getName();
            String msg = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" +
                    ChatColor.WHITE + "] " + ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);
            String staff = ChatColor.WHITE + "[" + ChatColor.AQUA +
                    sname + ChatColor.WHITE + "] " + ChatColor.GREEN +
                    ChatColor.translateAlternateColorCodes('&', message);
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (dashboard.getPlayer(tp.getUniqueId()).getRank().getRankId() >= Rank.KNIGHT.getRankId()) {
                    tp.sendMessage(staff);
                } else {
                    tp.sendMessage(msg);
                }
            }
            return;
        }
        player.sendMessage(ChatColor.RED + "/b [Message]");
    }
}