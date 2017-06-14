package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Server;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.packets.dashboard.PacketOnlineCount;
import network.palace.dashboard.packets.dashboard.PacketSendToServer;
import network.palace.dashboard.packets.dashboard.PacketTargetLobby;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Marc on 7/14/16
 */
public class ServerUtil {
    private HashMap<String, Server> servers = new HashMap<>();
    private int lastCount = 0;
    private int lastServerCount = 0;

    private Map<String, Integer> mutedServers = new HashMap<>();

    public ServerUtil() {
        Dashboard dashboard = Launcher.getDashboard();
        loadServers();
        /*
         * Online Player Count Timer
         */
        new Timer().schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                try {
                    i++;
                    int count = dashboard.getOnlinePlayers().size();
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
                lobbies.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
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
                if (server == null || dashboard.getTargetServer().equals(server.getName())) {
                    return;
                }
                dashboard.setTargetServer(server.getName());
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

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                List<String> expiredServers = mutedServers.entrySet().stream().filter(entry -> entry.getValue().equals(30)).map(Map.Entry::getKey).collect(Collectors.toList());
                expiredServers.forEach(server -> {
                    mutedServers.remove(server);
                    Launcher.getDashboard().getModerationUtil().displayServerMute(server, false);
                });
                new HashMap<>(mutedServers).forEach((server, time) -> mutedServers.put(server, time + 1));
            }
        }, 0, 60000);
    }

    public boolean isMuted(String server) {
        return mutedServers.containsKey(server);
    }

    public void muteServer(String server) {
        mutedServers.put(server, 0);
        Launcher.getDashboard().getModerationUtil().displayServerMute(server, true);
    }

    public void unmuteServer(String server) {
        if (mutedServers.containsKey(server)) {
            mutedServers.remove(server);
            Launcher.getDashboard().getModerationUtil().displayServerMute(server, false);
        }
    }

    public List<Server> getServers() {
        return new ArrayList<>(servers.values());
    }

    private void loadServers() {
        servers.clear();
        Dashboard dashboard = Launcher.getDashboard();
        try (Connection connection = dashboard.getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT name,address,port,park,type FROM " +
                    (dashboard.isTestNetwork() ? "playground" : "") + "servers;");
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                servers.put(result.getString("name"), new Server(result.getString("name"), result.getString("address"),
                        result.getInt("port"), result.getInt("park") == 1, 0, result.getString("type")));
            }
            result.close();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
            dashboard.getLogger().error("Error loading servers, shutting Dashboard!");
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
        if (player != null) {
            player.send(packet);
        }
    }

    public void sendPlayerByType(Player player, String type) {
        if (player == null) {
            return;
        }
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