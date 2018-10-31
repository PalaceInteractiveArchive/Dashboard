package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.arcade.PacketGameStatus;
import network.palace.dashboard.packets.audio.PacketContainer;
import network.palace.dashboard.packets.dashboard.*;
import network.palace.dashboard.packets.inventory.PacketInventoryContent;
import network.palace.dashboard.packets.inventory.Resort;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Marc on 7/14/16
 */
public class ServerUtil {
    private HashMap<String, Server> servers = new HashMap<>();
    private int lastCount = 0;
    private int lastServerCount = 0;
    private int lastParks = 0;
    private int lastCreative = 0;
    private int lastArcade = 0;
    private HashMap<String, Integer> lastHubs = new HashMap<>();
    private TimerTask lobbyDataTask;

    private Map<String, UUID> mutedServers = new HashMap<>();

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
        /*
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
        /*
         * Muted Servers Timer
         */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Map.Entry<String, UUID> entry : mutedServers.entrySet()) {
                    Player tp = dashboard.getPlayer(entry.getValue());
                    if (tp != null) continue;
                    String server = entry.getKey();
                    mutedServers.remove(server);
                    dashboard.getModerationUtil().displayServerMute(server, false);
                }
            }
        }, 0, 10 * 1000);
        /*
         * Lobby Data Timer
         */
        lobbyDataTask = new TimerTask() {
            @Override
            public void run() {
                int parks = 0;
                int creative = 0;
                int arcade = 0;
                HashMap<String, Integer> hubs = new HashMap<>();
                for (Server s : getServers()) {
                    if (!s.isOnline()) continue;
                    if (s.getName().startsWith("Creative")) {
                        creative += s.getCount();
                    } else if (s.getName().startsWith("Arcade") || s.getName().matches(WebSocketServerHandler.MINIGAME_REGEX)) {
                        arcade += s.getCount();
                    } else if (s.getName().startsWith("Hub")) {
                        hubs.put(s.getName(), s.getCount());
                    } else if (s.isPark()) {
                        parks += s.getCount();
                    }
                }

                if (parks == lastParks && creative == lastCreative && arcade == lastArcade) {
                    boolean stop = true;
                    if (hubs.isEmpty() && lastHubs.isEmpty()) {
                        stop = false;
                    }
                    for (Map.Entry<String, Integer> entry : hubs.entrySet()) {
                        if (!lastHubs.containsKey(entry.getKey()) ||
                                !lastHubs.get(entry.getKey()).equals(entry.getValue())) {
                            stop = false;
                            break;
                        }
                    }
                    if (stop) return;
                }

                lastParks = parks;
                lastCreative = creative;
                lastArcade = arcade;
                lastHubs = hubs;
                PacketLobbyData packet = new PacketLobbyData(parks, creative, arcade, hubs);
                for (Server s : getServers()) {
                    if (s.getName().startsWith("Hub") || s.getName().startsWith("Arcade")) {
                        DashboardSocketChannel channel = Dashboard.getInstance(s.getName());
                        if (channel != null) channel.send(packet);
                    }
                }
            }
        };
        new Timer().schedule(lobbyDataTask, 5000, 5 * 1000);
        /*
         * Game Server Timer
         */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                List<Server> arcades = getServers().stream().filter(s -> s.getName().startsWith("Arcade")).collect(Collectors.toList());
                for (Server s : getServers()) {
                    if (!s.getName().matches(WebSocketServerHandler.MINIGAME_REGEX) || !s.isGameNeedsUpdate()) continue;
                    PacketGameStatus status = new PacketGameStatus(s.getGameState(), s.getCount(), s.getGameMaxPlayers(), s.getName());
                    s.setGameNeedsUpdate(false);
                    for (Server arcade : arcades) {
                        DashboardSocketChannel socketChannel = Dashboard.getInstance(arcade.getName());
                        if (socketChannel != null) {
                            socketChannel.send(status);
                        }
                    }
                }
            }
        }, 0, 1000);
    }

    public void serverSwitchEvent(UUID uuid, String target) {
        try {
            Dashboard dashboard = Launcher.getDashboard();
            final Player tp = dashboard.getPlayer(uuid);
            if (tp == null) {
                if (dashboard.hasPlayer(uuid)) {
                    dashboard.setRegisteringPlayerServer(uuid, target);
                }
                return;
            }
            // New connection
            if (dashboard.getServerUtil().getServer(tp.getServer()) == null) {
                dashboard.getServerUtil().getServer(target).changeCount(1);
                tp.setServer(target);
                if (tp.isDisabled()) {
                    PacketDisablePlayer dis = new PacketDisablePlayer(tp.getUniqueId(), true);
                    DashboardSocketChannel socketChannel = Dashboard.getInstance(target);
                    if (socketChannel == null) return;
                    socketChannel.send(dis);
                    /*
                     * /staff login pw
                     * /staff change oldpw newpw
                     */
                }
//                    if (dashboard.getServer(target).isPark() && Dashboard.getInstance(target) != null) {
//                        tp.setInventoryUploaded(false);
//                        PacketInventoryStatus update = new PacketInventoryStatus(tp.getUniqueId(), 1);
//                        sendInventoryUpdate(target, update);
//                    }
                return;
            }
            // Going to Park server
            Server server = dashboard.getServer(target);
            if (server == null) server = getServerByType("Hub");
            if (server.isPark()) {
                if (tp.isSendInventoryOnJoin() && server.isInventory()) {
                    tp.setSendInventoryOnJoin(false);
                    Resort resort = Resort.fromServer(target);
                    dashboard.getSchedulerManager().runAsync(() -> {
                        try {
                            ResortInventory inv = dashboard.getInventoryUtil().getInventory(tp.getUniqueId(), resort);
                            PacketInventoryContent content = new PacketInventoryContent(tp.getUniqueId(), resort,
                                    inv.getBackpackJSON(), inv.getBackpackHash(), inv.getBackpackSize(),
                                    inv.getLockerJSON(), inv.getLockerHash(), inv.getLockerSize(),
                                    inv.getHotbarJSON(), inv.getHotbarHash());
                            dashboard.getInventoryUtil().cacheInventory(tp.getUniqueId(), content);
                            DashboardSocketChannel socketChannel = Dashboard.getInstance(target);
                            if (socketChannel == null) {
                                return;
                            }
                            socketChannel.send(content);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                if (!server.isInventory()) {
                    tp.setSendInventoryOnJoin(true);
                }
                if (tp.isPendingWarp()) {
                    tp.chat("/warp " + tp.getWarp());
                    tp.setPendingWarp(false);
                }
            }
            if (server.getServerType().equals("Creative")) {
                tp.sendServerIgnoreList(target);
            }
            network.palace.dashboard.packets.audio.PacketServerSwitch change =
                    new network.palace.dashboard.packets.audio.PacketServerSwitch(target);
            PacketContainer audio = new PacketContainer(uuid, change.getJSON().toString());
            for (Object o : WebSocketServerHandler.getGroup()) {
                DashboardSocketChannel dash = (DashboardSocketChannel) o;
                if (!dash.getType().equals(PacketConnectionType.ConnectionType.AUDIOSERVER)) {
                    continue;
                }
                dash.send(audio);
            }
            if (!tp.getServer().equalsIgnoreCase("unknown")) {
                dashboard.getServerUtil().getServer(tp.getServer()).changeCount(-1);
            }
            dashboard.getServerUtil().getServer(target).changeCount(1);
            tp.setServer(target);

            // Check if the destination is a minigame server
            if (target.matches(WebSocketServerHandler.MINIGAME_REGEX)) {
                Party party = dashboard.getPartyUtil().findPartyForPlayer(tp);
                if (party != null) {
                    // Are they the leader?
                    if (party.getLeader().getUniqueId().equals(tp.getUniqueId())) {
                        // Yup, so send all the party members.
                        party.warpToLeader();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isMuted(String server) {
        return mutedServers.containsKey(server);
    }

    public void muteServer(UUID uuid, String server) {
        mutedServers.put(server, uuid);
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
        try {
            List<Server> servers = dashboard.getMongoHandler().getServers(dashboard.isTestNetwork());
            for (Server s : servers) {
                this.servers.put(s.getName(), s);
            }
        } catch (Exception e) {
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
            if ((server.getUniqueId().equals(exclude)) || !server.isOnline()) {
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
            if ((server.getUniqueId().equals(exclude)) || !server.isOnline()) {
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

    public void runLobbyDataTask() {
        lastParks = 0;
        lastCreative = 0;
        lastArcade = 0;
        lastHubs = new HashMap<>();
        lobbyDataTask.run();
    }
}