package network.palace.dashboard.commands;

import network.palace.dashboard.handlers.chat.ChatColor;
import network.palace.dashboard.handlers.DashboardCommand;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

/**
 * Created by Marc on 10/8/16
 */
public class ProcessesCommand extends DashboardCommand {

    public ProcessesCommand() {
        super(Rank.DEVELOPER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        StringBuilder msg = new StringBuilder(ChatColor.YELLOW + "Processes connected to Dashboard:");
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            String type = "";
            String ip = dash.remoteAddress().getAddress().getHostAddress() + ":" + dash.remoteAddress().getPort();
            switch (dash.getType()) {
                case BUNGEECORD:
                    type = "BungeeCord - " + ip;
                    break;
                case DAEMON:
                    type = "Daemon - " + ip;
                    break;
                case WEBCLIENT:
                    type = "Web Client - " + ip;
                    break;
                case INSTANCE:
                    type = dash.getServerName() + " - " + ip;
                    break;
                case AUDIOSERVER:
                    type = "The Audio Server - " + ip;
                    break;
                case UNKNOWN:
                    type = "Unknown - " + ip;
                    break;
            }
            msg.append(ChatColor.GREEN).append("\n- ").append(type);
        }
        player.sendMessage(msg.toString());
    }
}