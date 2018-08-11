package network.palace.dashboard.commands;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;

public class SpamIPCommand extends DashboardCommand {

    public SpamIPCommand() {
        super(Rank.SRMOD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        Dashboard dashboard = Launcher.getDashboard();
        switch (args.length) {
            case 2: {
                if (!args[0].equalsIgnoreCase("remove")) {
                    help(player);
                    break;
                }
                String ip = args[1];
                dashboard.getMongoHandler().removeSpamIPWhitelist(ip);
                dashboard.getModerationUtil().announceSpamWhitelistRemove(ip);
                break;
            }
            case 3: {
                if (!args[0].equalsIgnoreCase("whitelist")) {
                    help(player);
                    break;
                }
                String ip = args[1];
                int limit;
                try {
                    limit = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + args[2] + " is not a number!");
                    break;
                }
                SpamIPWhitelist whitelist = new SpamIPWhitelist(ip, limit);
                dashboard.getMongoHandler().addSpamIPWhitelist(whitelist);
                dashboard.getModerationUtil().announceSpamWhitelistAdd(whitelist);
                break;
            }
            default: {
                help(player);
                break;
            }
        }
    }

    private void help(Player player) {
        player.sendMessage(ChatColor.GREEN + "Spam IP Commands:");
        player.sendMessage(ChatColor.AQUA + "- /spamip whitelist [ip] [limit] " +
                ChatColor.GREEN + "- Whitelist an IP from spam protection");
        player.sendMessage(ChatColor.AQUA + "- /spamip remove [ip] " + ChatColor.GREEN +
                "- Remove a Spam IP Whitelist");
    }
}
