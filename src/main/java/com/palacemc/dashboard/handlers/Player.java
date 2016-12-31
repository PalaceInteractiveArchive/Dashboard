package com.palacemc.dashboard.handlers;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.dashboard.PacketMention;
import com.palacemc.dashboard.packets.dashboard.PacketMessage;
import com.palacemc.dashboard.packets.dashboard.PacketPlayerChat;
import com.palacemc.dashboard.packets.dashboard.PacketPlayerDisconnect;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.UUID;

/**
 * Created by Marc on 7/14/16
 */
public class Player {
    @Getter @Setter private UUID uuid;
    @Getter @Setter private String username;
    @Getter @Setter private Rank rank = Rank.SETTLER;
    @Getter @Setter private String address;
    @Getter @Setter private String server;
    @Getter @Setter private UUID bungeeID;
    @Getter @Setter private boolean newGuest = false;
    @Getter @Setter private Timer tutorial = null;
    @Getter @Setter private boolean toggled = true;
    @Getter @Setter private boolean mentions = true;
    @Getter @Setter private long loginTime = System.currentTimeMillis();
    @Getter @Setter private UUID reply;
    @Getter @Setter private Mute mute;
    @Getter @Setter private HashMap<UUID, String> friends;
    @Getter @Setter private HashMap<UUID, String> requests;
    @Getter @Setter private boolean kicking;
    @Getter @Setter private int audioAuth = -1;
    @Getter @Setter private boolean recieveMessages = true;
    @Getter @Setter private String pack = "none";
    @Getter @Setter private String warp = "";
    @Getter @Setter private boolean pendingWarp = false;
    @Getter @Setter private boolean inventoryUploaded;
    @Getter @Setter private long onlineTime = 0;
    @Getter @Setter private String channel = "all";
    @Getter @Setter private long afkTime = System.currentTimeMillis();
    @Getter @Setter private boolean isAFK = false;

    private Dashboard dashboard = Launcher.getDashboard();

    public Player(UUID uuid, String username, String address, String server, UUID bungeeID) {
        this.uuid = uuid;
        this.username = username;
        this.address = address;
        this.server = server;
        this.bungeeID = bungeeID;
    }

    public void sendMessage(String msg) {
        PacketMessage packet = new PacketMessage(uuid, msg);
        send(packet);
    }

    /**
     * Send packet to player's BungeeCord
     *
     * @param packet Packet to send
     */
    public void send(BasePacket packet) {
        if (packet == null) {
            return;
        }
        dashboard.getBungee(bungeeID).send(packet.getJSON().toString());
    }

    public void chat(String msg) {
        PacketPlayerChat packet = new PacketPlayerChat(uuid, msg);
        send(packet);
    }

    public void setReply(UUID uuid) {
        this.reply = uuid;
    }

    public UUID getReply() {
        return reply;
    }

    public void kickPlayer(String reason) {
        kicking = true;
        PacketPlayerDisconnect packet = new PacketPlayerDisconnect(uuid, reason);
        send(packet);
    }

    public int setAudioAuth() {
        this.audioAuth = new Random().nextInt(100000);
        return audioAuth;
    }

    public void resetAudioAuth() {
        this.audioAuth = -1;
    }

    public void mention() {
        try {
            PacketMention packet = new PacketMention(uuid);
            dashboard.getInstance(server).send(packet);
        } catch (Exception ignored) {
        }
    }

    public void afkAction() {
        afkTime = System.currentTimeMillis();
    }
    public boolean hasFriendToggledOff() {
        return toggled;
    }

    public void setHasFriendToggled(boolean bool) {
        toggled = bool;
    }
}