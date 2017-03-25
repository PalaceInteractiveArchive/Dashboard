package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.*;

/**
 * Created by Marc on 3/25/17.
 */
public class Commandchatstatus extends MagicCommand {

    public Commandchatstatus() {
        super(Rank.SQUIRE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Server s = Dashboard.getServer(player.getServer());
        boolean park = s.isPark();
        String name = "";
        int count = 0;
        boolean muted = false;
        if (park) {
            int c = 0;
            for (Server sr : Dashboard.getServers()) {
                if (!sr.isPark()) {
                    continue;
                }
                count += sr.getCount();
                if (sr.getCount() != 0) {
                    c++;
                }
            }
            name = "ParkChat (" + c + " servers)";
            muted = Dashboard.chatUtil.isChatMuted("ParkChat");
        } else {
            name = s.getName();
            count = s.getCount();
            muted = Dashboard.chatUtil.isChatMuted(s.getName());
        }
        player.sendMessage(ChatColor.GREEN + "Name: " + ChatColor.YELLOW + name + "\n" + ChatColor.GREEN +
                "Players: " + ChatColor.YELLOW + count + "\n" + ChatColor.GREEN + "Status: " +
                (muted ? (ChatColor.RED + "Muted") : (ChatColor.YELLOW + "Unmuted")));
    }
}
