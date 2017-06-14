package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Commandb extends MagicCommand {

    public Commandb() {
        super(Rank.KNIGHT);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length > 0) {
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            String sname = player.getUsername();
            String msg = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" +
                    ChatColor.WHITE + "] " + ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message.toString());
            String staff = ChatColor.WHITE + "[" + ChatColor.AQUA +
                    sname + ChatColor.WHITE + "] " + ChatColor.GREEN +
                    ChatColor.translateAlternateColorCodes('&', message.toString());
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (dashboard.getPlayer(tp.getUniqueId()).getRank().getRankId() >= Rank.KNIGHT.getRankId()) {
                    tp.sendMessage(staff);
                } else {
                    tp.sendMessage(msg);
                }
            }
            return;
        }
        Runtime r = Runtime.getRuntime();
        try {
            Process p = r.exec("sensors");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String input;
            while ((input = in.readLine()) != null) {
                System.out.println(input);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.sendMessage(ChatColor.RED + "/b [Message]");
    }
}
