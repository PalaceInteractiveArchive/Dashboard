package com.palacemc.dashboard.commands;

import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.server.WebSocketServerHandler;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.MagicCommand;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.server.DashboardSocketChannel;

/**
 * Created by Marc on 10/8/16
 */
public class Commandprocesses extends MagicCommand {

    public Commandprocesses() {
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