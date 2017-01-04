package network.palace.dashboard.commands;

import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.server.WebSocketServerHandler;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.MagicCommand;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.server.DashboardSocketChannel;

/**
 * Created by Marc on 10/8/16
 */
public class CommandProcesses extends MagicCommand {

    public CommandProcesses() {
        super(Rank.WIZARD);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        String msg = ChatColor.YELLOW + "Processes connected to Dashboard:";
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            String type = "";
            String ip = dash.remoteAddress().getAddress().getHostAddress();
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
            msg += ChatColor.GREEN + "\n- " + type;
        }
        player.sendMessage(msg);
    }
}