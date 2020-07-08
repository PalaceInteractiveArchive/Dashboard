package network.palace.dashboard.queues;

import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Server;

import java.util.*;

public class VirtualQueue {
    // id of the queue
    // name of the queue
    @Getter private final String id, name;
    // number of players that fit in the holding area for the queue
    @Getter private final int holdingArea;
    // server the queue was created on
    @Getter private final Server server;

    // whether players can join the queue
    @Getter private boolean open = false;
    // the list of players in queue
    private final LinkedList<UUID> queue = new LinkedList<>();
    private final List<UUID> sendToServer = new ArrayList<>();

    @Getter @Setter private boolean updated = false;

    public VirtualQueue(String id, String name, int holdingArea, Server server) {
        this.id = id;
        this.name = name;
        this.holdingArea = holdingArea;
        this.server = server;
    }

    public void admit() {
        if (queue.isEmpty()) return;
        leaveQueue(queue.getFirst());
    }

    public void setOpen(boolean open) {
        if (this.open == open) return;
        this.open = open;
        updated = true;
        ListIterator<UUID> iterator = queue.listIterator();
        UUID uuid;
        int position = 1;
        String msg = open ? (ChatColor.GREEN + "The virtual queue " + name + ChatColor.GREEN + " has opened! You're in position #") :
                (ChatColor.AQUA + "The virtual queue " + name + ChatColor.AQUA + " has been closed! You're still in line, but you will lose your place if you leave the queue.");
        while (iterator.hasNext()) {
            uuid = iterator.next();
            position++;
            Player tp;
            if (uuid != null && ((tp = Launcher.getDashboard().getPlayer(uuid)) != null)) {
                tp.sendMessage(open ? (msg + position) : msg);
            }
        }
    }

    public boolean joinQueue(Player player) {
        if (!open) {
            player.sendMessage(ChatColor.RED + "The virtual queue " + name + ChatColor.RED + " is currently closed, sorry!");
            return false;
        }
        if (getPosition(player.getUniqueId()) >= 1) {
            player.sendMessage(ChatColor.RED + "You're already in the virtual queue " + name + "!");
            return false;
        }
        queue.add(player.getUniqueId());
        updated = true;
        player.sendMessage(ChatColor.GREEN + "You joined the virtual queue " + name +
                " in position #" + getPosition(player.getUniqueId()) + "!");
        if (getPosition(player.getUniqueId()) <= holdingArea) {
            player.sendMessage(ChatColor.GREEN + "");
        }
        return true;
    }

    public boolean leaveQueue(Player player, boolean message) {
        if (leaveQueue(player.getUniqueId())) {
            if (message) player.sendMessage(ChatColor.GREEN + "You have left the virtual queue " + name + "!");
            return true;
        } else {
            if (message) player.sendMessage(ChatColor.RED + "You aren't in the virtual queue " + name + "!");
            return false;
        }
    }

    public boolean leaveQueue(UUID uuid) {
        int position = queue.indexOf(uuid);
        if (position >= 0) {
            queue.remove(uuid);
            sendToServer.remove(uuid);
            updated = true;
            ListIterator<UUID> iterator = queue.listIterator(position);
            UUID playerInQueue;
            while (iterator.hasNext()) {
                playerInQueue = iterator.next();
                position++;
                Player tp;
                if (playerInQueue != null && ((tp = Launcher.getDashboard().getPlayer(playerInQueue)) != null)) {
                    sendPositionMessage(tp, position);
                }
            }
            return true;
        }
        return false;
    }

    public void sendPositionMessages() {
        ListIterator<UUID> iterator = queue.listIterator();
        UUID uuid;
        int position = 1;
        while (iterator.hasNext()) {
            uuid = iterator.next();
            Player tp;
            if (uuid != null && ((tp = Launcher.getDashboard().getPlayer(uuid)) != null)) {
                sendPositionMessage(tp, position);
            }
            position++;
        }
    }

    private void sendPositionMessage(Player player) {
        sendPositionMessage(player, getPosition(player.getUniqueId()));
    }

    private void sendPositionMessage(Player player, int pos) {
        if (pos >= 1) {
            player.sendMessage(ChatColor.GREEN + "You are in position #" + pos + " in the virtual queue " + name + "!");
        }
    }

    public int getPosition(UUID uuid) {
        return queue.indexOf(uuid) + 1;
    }

    public List<UUID> getHoldingAreaMembers() {
        return queue.subList(0, queue.size() < holdingArea ? queue.size() : holdingArea);
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(queue);
    }

    public List<UUID> getSendingToServer() {
        return new ArrayList<>(sendToServer);
    }

    /**
     * Mark this player as going to be sent to the server in the next timer cycle
     *
     * @param player the player
     */
    public void markAsSendingToServer(Player player) {
        if (sendToServer.contains(player.getUniqueId())) return;
        sendToServer.add(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "In " + ChatColor.AQUA + ChatColor.BOLD + "10 seconds " +
                ChatColor.GREEN + "you will be sent to " + ChatColor.AQUA + server.getName() + ChatColor.GREEN +
                " for the queue " + name + "...");
    }

    public void sendToServer(Player player) {
        sendToServer.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Sending you to " + ChatColor.AQUA + server.getName() +
                ChatColor.GREEN + " for the queue " + name + "...");
        Launcher.getDashboard().getServerUtil().sendPlayer(player, server);
    }
}
