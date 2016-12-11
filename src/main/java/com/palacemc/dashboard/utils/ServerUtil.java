package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Server;
import com.palacemc.dashboard.packets.dashboard.PacketConnectionType;
import com.palacemc.dashboard.packets.dashboard.PacketOnlineCount;
import com.palacemc.dashboard.packets.dashboard.PacketSendToServer;
import com.palacemc.dashboard.packets.dashboard.PacketTargetLobby;
import com.palacemc.dashboard.server.DashboardSocketChannel;
import com.palacemc.dashboard.server.WebSocketServerHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Marc on 7/14/16
 */
public class ServerUtil {
    private HashMap<String, Server> servers = new HashMap<>();
    private int lastCount = 0;
    private int lastServerCount = 0;

    public ServerUtil() throws IOException {
        loadServers();
        /**
         * Online Player Count Timer
         */
        new Timer().schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                try {
                    i++;
                    int count = Launcher.getDashboard().getOnlinePlayers().size();
                    PacketOnlineCount packet = new PacketOnlineCount(count);
                    if (count != lastCount) {
                        lastCount = count;
                        for (Object o : WebSocketServerHandler.getGroup()) {
                            DashboardSocketChannel dash = (DashboardSocketChannel) o;
                            if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                                continue;
                            }
                            dash.send(packet);
                        }
                    }
                    if (i < 10) {
                        return;
                    }
                    i = 0;
                    if (count != lastServerCount) {
                        lastServerCount = count;
                        for (Object o : WebSocketServerHandler.getGroup()) {
                            DashboardSocketChannel dash = (DashboardSocketChannel) o;
                            if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                                continue;
                            }
                            dash.send(packet);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }, 0L, 1000L);
        /**
         * Empty Lobby Timer
         */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                List<Server> lobbies = new ArrayList<>();

                for (Server s : getServers()) {
                    if (s.getServerType().equalsIgnoreCase("hub")) {
                        lobbies.add(s);
                    }
                }

                Collections.sort(lobbies, (s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
                Server server = null;

                for (Server s : lobbies) {
                    if (server == null) {
                        server = s;
                        continue;
                    }

                    if (((s.getCount() < server.getCount()) || !server.isOnline()) && s.isOnline()) {
                        server = s;
                    }
                }

                if (server == null || Launcher.getDashboard().getTargetServer().equals(server.getName())) {
                    return;
                }

                Launcher.getDashboard().setTargetServer(server.getName());
                PacketTargetLobby packet = new PacketTargetLobby(server.getName());
                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                        continue;
                    }
                    dash.send(packet);
                }
            }
        }, 0, 5000);
    }

    public List<Server> getServers() {
        return new ArrayList<>(servers.values());
    }

    private void loadServers() {
        servers.clear();

        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            //TODO Change this back to the regular table
            PreparedStatement sql = connection.prepareStatement("SELECT name,address,port,park,type FROM " +
                    (Launcher.getDashboard().isTestNetwork() ? "playground" : "") + "servers;");
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                servers.put(result.getString("name"), new Server(result.getString("name"), result.getString("address"),
                        result.getInt("port"), result.getInt("park") == 1, 0, result.getString("type")));
            }
            result.close();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Launcher.getDashboard().getLogger().error("Error loading servers, shutting Dashboard!");
            System.exit(0);
        }
    }

    public Server getServer(String server) {
        return servers.get(server);
    }

    public Server getServerByType(String type) {
        return getServerByType(type, null);
    }

    public Server getServerByType(String type, UUID exclude) {
        Server s = null;
        List<Server> servers = new ArrayList<>(this.servers.values());
        for (Server server : servers) {
            if ((exclude != null && server.getUuid().equals(exclude)) || !server.isOnline()) {
                continue;
            }
            if (server.getServerType().equalsIgnoreCase(type)) {
                if (s == null) {
                    s = server;
                    continue;
                }
                if (server.getCount() < s.getCount()) {
                    s = server;
                }
            }
        }
        return s;
    }

    public void sendPlayer(Player player, String server) {
        PacketSendToServer packet = new PacketSendToServer(player.getUuid(), server);
        player.send(packet);
    }

    public void sendPlayerByType(Player player, String type) {
        Server s = getServerByType(type);
        if (s == null) {
            if (player.isPendingWarp()) {
                player.setPendingWarp(false);
            }
            player.sendMessage(ChatColor.RED + "No '" + type + "' server is available right now! Please try again soon.");
            return;
        }
        sendPlayer(player, s.getName());
    }

    public void addServer(Server server) {
        servers.put(server.getName(), server);
    }

    public void removeServer(String name) {
        servers.remove(name);
    }

    public Server getEmptyParkServer(UUID exclude) {
        Server s = null;
        List<Server> servers = new ArrayList<>(this.servers.values());
        for (Server server : servers) {
            if ((exclude != null && server.getUuid().equals(exclude)) || !server.isOnline()) {
                continue;
            }
            if (!server.isPark()) {
                continue;
            }
            if (s == null) {
                s = server;
                continue;
            }
            if (server.getCount() < s.getCount()) {
                s = server;
            }
        }
        return s;
    }
}