package network.palace.dashboard.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ClickEvent;
import network.palace.dashboard.chat.ComponentBuilder;
import network.palace.dashboard.chat.HoverEvent;
import network.palace.dashboard.packets.arcade.GameState;

import java.util.*;

/**
 * Created by Marc on 5/29/16
 */
public class Server {
    private UUID uuid = UUID.randomUUID();
    @Getter private String name;
    @Getter private String address;
    @Getter private boolean park;
    @Getter @Setter private int count;
    @Getter @Setter private int gameMaxPlayers;
    @Getter private String serverType;
    @Getter @Setter private boolean online = false;
    @Getter @Setter private boolean inventory = false;
    @Getter @Setter private GameState gameState = GameState.LOBBY;
    @Getter @Setter private boolean gameNeedsUpdate = true;
    @Getter @Setter private boolean arcade = false;

    @Getter private ServerQueue serverQueue = new ServerQueue();

    public Server(String name, String address, boolean park, int count, String serverType) {
        this.name = name;
        this.address = address;
        this.park = park;
        this.count = count;
        this.serverType = serverType;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void changeCount(int i) {
        this.count += i;
    }

    public void emptyServer() {
        Server s = null;
        Dashboard dashboard = Launcher.getDashboard();
        for (Server server : dashboard.getServerUtil().getServers()) {
            if (server.getUniqueId().equals(getUniqueId())) {
                continue;
            }
            if (server.getServerType().equalsIgnoreCase("hub")) {
                if (s == null) {
                    s = server;
                    continue;
                }
                if (server.getCount() < s.getCount()) {
                    s = server;
                }
            }
        }
        Server server = s;
        dashboard.getOnlinePlayers().stream().filter(tp -> tp.getServer().equals(getName())).forEach(tp -> dashboard.getServerUtil().sendPlayer(tp, server));
    }

    public class ServerQueue {
        private List<ServerJoin> joinHistory = new ArrayList<>();
        private LinkedList<Player> joinQueue = new LinkedList<>();
        private long startQueue = -1;

        /**
         * Log a new join to this server
         *
         * @param uuid the player joining
         */
        public void handleJoin(UUID uuid) {
            joinHistory.add(new ServerJoin(uuid, System.currentTimeMillis()));
        }

        /**
         * Determine whether a queue needs to be started for this server
         *
         * @return true if there have been five or more connections to this server within the last five seconds, or if the queue is already started and has been on for less than twenty seconds
         */
        public boolean isQueueNeeded() {
            int count = 0;
            long currentTime = System.currentTimeMillis();
            // Queues must be enabled for at least 20 seconds when they're started
            if (startQueue != -1 && currentTime - startQueue <= 20 * 1000) return true;

            for (ServerJoin join : joinHistory) {
                if (currentTime - join.time <= 5 * 1000) count++;
                if (count >= 5) return true;
            }
            return false;
        }

        /**
         * Remove all join entries that happened more than five seconds ago
         */
        public void clearExpired() {
            Iterator<ServerJoin> i = joinHistory.iterator();
            long currentTime = System.currentTimeMillis();
            List<ServerJoin> remove = new ArrayList<>();
            while (i.hasNext()) {
                ServerJoin join = i.next();
                if (currentTime - join.time > 5 * 1000) remove.add(join);
            }
            joinHistory.removeAll(remove);
        }

        /**
         * Adds a player to the join queue for this server
         *
         * @param player the player
         * @return the position the player is in
         */
        public int joinQueue(Player player) {
            // If queue was previously off, turn it on
            if (startQueue == -1) {
                startQueue = System.currentTimeMillis();
                Launcher.getDashboard().getLogger().info("Server queue enabled for " + getName());
            }
            joinQueue.add(player);
            return joinQueue.size();
        }

        /**
         * Determine whether the queue timer needs to process the queue for this server
         *
         * @return true if the queue has at least one entry in it, false if not
         */
        public boolean hasQueue() {
            boolean empty = joinQueue.isEmpty();
            if (empty) {
                // If queue is empty and has existed for at least 20 seconds, turn it off
                if (startQueue != -1 && System.currentTimeMillis() - startQueue > 20000) {
                    startQueue = -1;
                    Launcher.getDashboard().getLogger().info("Server queue disabled for " + getName());
                }
                return false;
            } else {
                // Queue isn't empty, so keep it on
                return true;
            }
        }

        /**
         * Pull the next player from the queue
         *
         * @return the first player in the queue
         */
        public Player nextFromQueue() {
            return joinQueue.pop();
        }

        /**
         * Send out a status update to all players in the queue
         */
        public void statusUpdate() {
            int i = 1;
            for (Player tp : joinQueue) {
                tp.sendMessage(new ComponentBuilder("You are in position ").color(ChatColor.GREEN)
                        .append("#" + (i++)).color(ChatColor.YELLOW)
                        .append(" in queue to join ").color(ChatColor.GREEN)
                        .append(getName()).color(ChatColor.AQUA).append("\n")
                        .append("Click here to leave the queue").color(ChatColor.RED)
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Leave the queue to join " + getName()).color(ChatColor.YELLOW).create()))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/leavedashqueue")).create());
            }
        }

        /**
         * Leave the server queue
         *
         * @param player the player leaving
         * @return true if was in queue, false if not
         */
        public boolean leaveQueue(Player player) {
            return joinQueue.remove(player);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ServerJoin {
        private UUID player;
        private long time;
    }
}