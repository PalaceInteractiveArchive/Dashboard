package network.palace.dashboard.commands;

import io.socket.parser.Packet;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ClickEvent;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.chat.HoverEvent;
import network.palace.dashboard.discordSocket.DiscordUserInfo;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.mongo.MongoHandler;
import network.palace.dashboard.packets.dashboard.PacketLink;
import network.palace.dashboard.utils.DiscordUtil;

/**
 * @author Innectic
 * @since 2/18/2017
 */
public class DiscordCommand extends DashboardCommand {

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length == 0 ) {
            boolean isLinked = Launcher.getDashboard().getMongoHandler().verifyDiscordLink(player.getUuid());
            if (isLinked) {
                player.sendMessage(ChatColor.GREEN + "Hey! " + player.getUsername() + " you have a linked discord account with the username: " + ChatColor.YELLOW + ChatColor.BOLD + " JohnSmith#0000");
            } else {
                player.sendMessage(ChatColor.GREEN + "Hey! " + player.getUsername() + " you haven't yet linked your discord account with our server. Please run: " +
                        ChatColor.YELLOW + ChatColor.BOLD + "/discord link");
            }
        } else if (args.length >= 2 && args[0].equals("link")) {
            player.sendMessage(new ComponentBuilder("\nClick to link your discord account\n")
            .color(ChatColor.YELLOW).underlined(false).bold(true)
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "27\n" +
                    "https://discord.com/api/oauth2/authorize?response_type=code&client_id=543141358496383048&scope=identify&state=" + player.getUuid().toString() + "&redirect_uri=https://internal-api.palace.network/"))
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Opens a browser window to start the discord linking process").color(ChatColor.GREEN).create())).create());
        }
    }
}
