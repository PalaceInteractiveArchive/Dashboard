package network.palace.dashboard.utils;

import lombok.Getter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.Party;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.ResortInventory;
import network.palace.dashboard.handlers.Server;
import network.palace.dashboard.packets.audio.PacketContainer;
import network.palace.dashboard.packets.dashboard.*;
import network.palace.dashboard.packets.inventory.PacketInventoryContent;
import network.palace.dashboard.packets.inventory.Resort;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;

import java.util.*;

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
    @Getter private boolean serverQueuesEnabled = true;

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
        }, 0, 5 * 1000);
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
        new Timer().schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                servers.values().stream()
                        .filter(s -> s.getServerQueue().hasQueue())
                        .forEach(server -> {
                            Server.ServerQueue queue = server.getServerQueue();
                            if (i % 2 == 0) queue.clearExpired();
                            if (queue.hasQueue()) {
                                if (i == 0) queue.statusUpdate();
                                Player tp = queue.nextFromQueue();
                                if (tp != null) {
                                    tp.sendMessage(ChatColor.GREEN + "You're up! Sending you to " + ChatColor.YELLOW + server.getName() + "...");
                                    sendPlayerDirect(tp, server.getName());
                                }
                            }
                        });
                i++;
                if (i >= 5) {
                    i = 0;
                }
            }
        }, 10 * 1000L, 1000L);
        /*
         * Game Server Timer
         */
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                List<Server> arcades = getServers().stream().filter(Server::isArcade).collect(Collectors.toList());
//                for (Server s : getServers()) {
//                    if (!s.getName().matches(WebSocketServerHandler.MINIGAME_REGEX) || !s.isGameNeedsUpdate()) continue;
//                    PacketGameStatus status = new PacketGameStatus(s.getGameState(), s.getCount(), s.getGameMaxPlayers(), s.getName());
//                    s.setGameNeedsUpdate(false);
//                    for (Server arcade : arcades) {
//                        DashboardSocketChannel socketChannel = Dashboard.getInstance(arcade.getName());
//                        if (socketChannel != null) {
//                            socketChannel.send(status);
//                        }
//                    }
//                }
//            }
//        }, 0, 1000);
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
            Server server = dashboard.getServer(target);
            if (server == null) server = getServerByType("Hub");
            if (server.isInventory()) {
                //Target is a Park server, check if we need to send inventory data

                if (!dashboard.getServer(tp.getServer()).isInventory() || tp.isSendInventoryOnJoin()) {
                    //Either current server is not a Park (from Hub to Park)
                    //or going Park to Park and data already stored and waiting to be sent

                    dashboard.getSchedulerManager().runAsync(() -> {
                        try {
                            Resort resort = Resort.fromServer(target);
                            ResortInventory inv = dashboard.getInventoryUtil().getInventory(tp.getUniqueId(), resort);
                            PacketInventoryContent content = new PacketInventoryContent(tp.getUniqueId(), resort,
                                    inv.getBackpackJSON(), inv.getBackpackHash(), inv.getBackpackSize(),
                                    inv.getLockerJSON(), inv.getLockerHash(), inv.getLockerSize(),
                                    inv.getBaseJSON(), inv.getBaseHash(), inv.getBuildJSON(), inv.getBuildHash());
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
            tp.setSendInventoryOnJoin(false);

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

    public void sendPlayer(Player player, String name) {
        sendPlayer(player, getServer(name));
    }

    public void sendPlayer(Player player, Server server) {
        if (player == null) return;
        if (serverQueuesEnabled) {
            String currentQueue = leaveServerQueue(player);
            if (currentQueue != null)
                player.sendMessage(ChatColor.AQUA + "You have left the queue to join " + ChatColor.YELLOW + currentQueue);

            if (!server.getName().startsWith("Hub")) {
                Server.ServerQueue queue = server.getServerQueue();
                queue.handleJoin(player.getUniqueId());
                // Add to queue if the queue is enabled, or if a queue needs to be enabled
                if (queue.hasQueue() || queue.isQueueNeeded()) {
                    int pos = queue.joinQueue(player);
                    player.sendMessage(ChatColor.YELLOW + server.getName() + ChatColor.GREEN +
                            " is very busy right now, so you have been placed in a queue to join it. You are in position " +
                            ChatColor.YELLOW + "#" + pos + ".");
                    return;
                }
            }
        }
        sendPlayerDirect(player, server.getName());
    }

    /**
     * Send a player to a server without any load/capacity checks
     *
     * @param player the player
     * @param server the server
     */
    private void sendPlayerDirect(Player player, String server) {
        player.send(new PacketSendToServer(player.getUniqueId(), server));
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
        sendPlayer(player, s);
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

    public String leaveServerQueue(Player player) {
        for (Server server : servers.values()) {
            Server.ServerQueue queue = server.getServerQueue();
            if (queue.hasQueue() && queue.leaveQueue(player)) return server.getName();
        }
        return null;
    }

    public boolean toggleServerQueueEnabled() {
        return (serverQueuesEnabled = !serverQueuesEnabled);
    }
}