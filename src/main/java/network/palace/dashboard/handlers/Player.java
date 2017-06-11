package network.palace.dashboard.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.dashboard.PacketMention;
import network.palace.dashboard.packets.dashboard.PacketMessage;
import network.palace.dashboard.packets.dashboard.PacketPlayerChat;
import network.palace.dashboard.packets.dashboard.PacketPlayerDisconnect;
import network.palace.dashboard.server.DashboardSocketChannel;

import java.util.HashMap;
import java.util.Timer;
import java.util.UUID;

/**
 * Created by Marc on 7/14/16
 */
public class Player {
    @Getter @Setter private UUID uuid;
    @Getter @Setter private String username = "";
    @Getter @Setter private Rank rank = Rank.SETTLER;
    @Getter private String address = "";
    @Getter @Setter private String server = "";
    @Getter @Setter private UUID bungeeID;
    @Getter private int mcVersion = 0;
    @Getter @Setter private boolean newGuest = false;
    @Getter @Setter private Timer tutorial = null;
    @Getter @Setter private boolean toggled = true;
    @Setter private boolean mentions = true;
    @Getter private long loginTime = System.currentTimeMillis();
    @Getter @Setter private UUID reply;
    @Setter private Mute mute;
    @Getter @Setter private HashMap<UUID, String> friends = new HashMap<>();
    @Getter @Setter private HashMap<UUID, String> requests = new HashMap<>();
    @Getter private boolean kicking = false;
    @Getter private String audioToken = "";
    @Setter private boolean receiveMessages = true;
    @Getter @Setter private String pack = "none";
    @Getter @Setter private String warp = "";
    @Getter @Setter private boolean pendingWarp = false;
    @Getter @Setter private long onlineTime = 0;
    @Getter @Setter private String channel = "all";
    @Getter private long afkTime = System.currentTimeMillis();
    @Getter @Setter private boolean isAFK = false;
    @Getter @Setter private boolean disabled = false;
    @Getter @Setter private boolean sendInventoryOnJoin = true;

    public Player(UUID uuid, String username, String address, String server, UUID bungeeID, int mcVersion) {
        this.uuid = uuid;
        this.username = username;
        this.address = address;
        this.server = server;
        this.bungeeID = bungeeID;
        this.mcVersion = mcVersion;
    }

    public void sendMessage(String msg) {
        PacketMessage packet = new PacketMessage(uuid, msg);
        send(packet);
    }

    /**
     * Send packet to player's BungeeCord
     *
     * @param packet the packet to send
     */
    public void send(BasePacket packet) {
        if (packet == null) {
            return;
        }
        DashboardSocketChannel bungee = Dashboard.getBungee(bungeeID);
        if (bungee == null) return;
        bungee.send(packet.getJSON().toString());
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void chat(String msg) {
        PacketPlayerChat packet = new PacketPlayerChat(uuid, msg);
        send(packet);
    }

    public void kickPlayer(String reason) {
        kicking = true;
        PacketPlayerDisconnect packet = new PacketPlayerDisconnect(uuid, reason);
        send(packet);
    }

    public Mute getMute() {
        if (mute == null) {
            return new Mute(uuid, username, false, System.currentTimeMillis(), "", "");
        }
        return mute;
    }

    public String setAudioToken() {
        this.audioToken = Launcher.getDashboard().getRandomToken();
        return audioToken;
    }

    public void resetAudioToken() {
        this.audioToken = "";
    }

    public void mention() {
        try {
            PacketMention packet = new PacketMention(uuid);
            Dashboard.getInstance(server).send(packet);
        } catch (Exception ignored) {
        }
    }

    /**
     * Get the name of the player
     *
     * @return the name of the player
     * @deprecated Use `getUsername` instead.
     */
    public String getName() {
        return username;
    }

    public boolean hasMentions() {
        return mentions;
    }

    public void afkAction() {
        afkTime = System.currentTimeMillis();
    }

    public boolean hasFriendToggledOff() {
        return toggled;
    }

    public boolean canRecieveMessages() {
        return receiveMessages;
    }
}