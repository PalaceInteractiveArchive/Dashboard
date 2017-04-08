package network.palace.dashboard.commands;

import network.palace.dashboard.discordSocket.DiscordUserInfo;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.packets.dashboard.PacketLink;

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
        if (args.length < 2) {
            PacketLink packet = new PacketLink(player.getUniqueId(), "https://palace.network/Discord",
                    "Click for more information about Discord!", ChatColor.YELLOW, true);
            player.send(packet);
        } else if (args.length == 2 && args[0].equals("link")) {
            StringBuilder fullName = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                fullName.append(args[i]);
            }
            if (fullName.toString().contains("#") && fullName.toString().matches("(.*)#(\\d+)")) {
                DiscordUserInfo userInfo = new DiscordUserInfo(fullName.toString(), player.getName(), player.getUniqueId().toString(), player.getRank().toString());
                SocketConnection.sendLink(userInfo);
            } else {
                player.sendMessage(ChatColor.DARK_RED + "Please specify a valid Discord ID!");
            }
        } else {
            PacketLink packet = new PacketLink(player.getUniqueId(), "https://palace.network/Discord",
                    "Click for more information about Discord!", ChatColor.YELLOW, true);
            player.send(packet);
        }
    }
}
