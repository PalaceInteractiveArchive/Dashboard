package network.palace.dashboard.commands;

import network.palace.dashboard.discordSocket.DiscordUserInfo;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;

/**
 * @author Innectic
 * @since 2/18/2017
 */
public class CommandDiscord extends MagicCommand {

    public CommandDiscord() {
        super(Rank.SETTLER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length >= 1) {
            if (args[0].contains("#") && args[0].matches("(.+)#(\\d+)")) {
                DiscordUserInfo userInfo = new DiscordUserInfo(args[0], player.getName(), player.getUniqueId().toString(), player.getRank().toString());
                SocketConnection.sendLink(userInfo);
            } else {
                player.sendMessage(ChatColor.DARK_RED + "Please specify a discord id!");
            }
        } else {
            player.sendMessage(ChatColor.DARK_RED + "Please specify a discord id!");
        }
    }
}
