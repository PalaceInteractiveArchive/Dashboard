package network.palace.dashboard.commands;

import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.discordSocket.DiscordCacheInfo;
import network.palace.dashboard.discordSocket.DiscordUserInfo;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketLink;
import network.palace.dashboard.utils.DiscordUtil;

/**
 * @author Innectic
 * @since 2/18/2017
 */
public class DiscordCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            PacketLink packet = new PacketLink(player.getUniqueId(), "https://palnet.us/Discord",
                    "Click for more information about Discord!", ChatColor.YELLOW, true);
            player.send(packet);
        } else if (args.length == 1 && args[0].equals("link")) {
            int discordPin = DiscordUtil.generatePin(player, 10000, 99999);

            player.sendMessage(ChatColor.GREEN + "Your discord verification pin is " + ChatColor.YELLOW + ChatColor.BOLD + discordPin);

//            StringBuilder fullName = new StringBuilder();
//            for (int i = 1; i < args.length; i++) {
//                fullName.append(args[i]);
//            }
//            if (fullName.toString().contains("#") && fullName.toString().matches("(.*)#(\\d+)")) {
//                DiscordUserInfo userInfo = new DiscordUserInfo(fullName.toString(), player.getUsername(), player.getUniqueId().toString(), player.getRank().toString());
//                if (SocketConnection.sendLink(userInfo)) {
//                    player.sendMessage(ChatColor.GREEN + "");
//                }
//            } else {
//                player.sendMessage(ChatColor.DARK_RED + "Please specify a valid Discord ID!");
//            }
        } else {
            PacketLink packet = new PacketLink(player.getUniqueId(), "https://palnet.us/Discord",
                    "Click for more information about Discord!", ChatColor.YELLOW, true);
            player.send(packet);
        }
    }
}
