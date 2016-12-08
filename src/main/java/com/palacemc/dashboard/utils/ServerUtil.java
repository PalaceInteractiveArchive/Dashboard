package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Dashboard;
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
                    int count = Dashboard.getOnlinePlayers().size();
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
                Collections.sort(lobbies, new Comparator<Server>() {
                    @Override
                    public int compare(final Server s1, final Server s2) {
                        return s1.getName().compareToIgnoreCase(s2.getName());
                    }
                });
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
                if (server == null || Dashboard.getTargetServer().equals(server.getName())) {
                    return;
                }
                Dashboard.setTargetServer(server.getName());
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
        /**
         * Game Server Timer
         new Timer().schedule(new TimerTask() {
        @Override public void run() {
        for (Server s : getServers()) {
        if (s.getName().toLowerCase().matches("[a-z]\\d{1,3}") && s.isOnline()) {
        PacketGameStatus packet = new PacketGameStatus(s.getName(), s.getCount(), s.isOnline() ?
        "online" : "offline");
        }
        }
        }
        }, 0, 2000);
         */
    }

    public List<Server> getServers() {
        return new ArrayList<>(servers.values());
    }

    private void loadServers() {
        servers.clear();
        try (Connection connection = Dashboard.sqlUtil.getConnection()) {
            //TODO Change this back to the regular table
            PreparedStatement sql = connection.prepareStatement("SELECT name,address,port,park,type FROM " +
                    (Dashboard.isTestNetwork() ? "playground" : "") + "servers;");
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                servers.put(result.getString("name"), new Server(result.getString("name"), result.getString("address"),
                        result.getInt("port"), result.getInt("park") == 1, 0, result.getString("type")));
            }
            result.close();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Dashboard.getLogger().error("Error loading servers, shutting Dashboard!");
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
            if ((exclude != null && server.getUniqueId().equals(exclude)) || !server.isOnline()) {
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
        PacketSendToServer packet = new PacketSendToServer(player.getUniqueId(), server);
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
            if ((exclude != null && server.getUniqueId().equals(exclude)) || !server.isOnline()) {
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