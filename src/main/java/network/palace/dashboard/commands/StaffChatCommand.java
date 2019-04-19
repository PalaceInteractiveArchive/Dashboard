package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

public class StaffChatCommand extends DashboardCommand {

    public StaffChatCommand() {
        super(Rank.TRAINEE);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/sc [Message]");
            return;
        }
        String message = String.join(" ", args);
        String response;
        Rank rank = player.getRank();
        SponsorTier tier = player.getSponsorTier();

        response = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " + tier.getChatTag(true) +
                rank.getFormattedName() + " " + ChatColor.GRAY + player.getUsername() + ": " + ChatColor.WHITE +
                ChatColor.translateAlternateColorCodes('&', message);
        dashboard.getChatUtil().staffChatMessage(response);
        dashboard.getChatUtil().logMessage(player.getUniqueId(), "/sc " + player.getUsername() + " " + message);
    }
}