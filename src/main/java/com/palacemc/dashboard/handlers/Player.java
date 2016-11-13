package com.palacemc.dashboard.handlers;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.dashboard.PacketMessage;
import com.palacemc.dashboard.packets.dashboard.PacketMention;
import com.palacemc.dashboard.packets.dashboard.PacketPlayerChat;
import com.palacemc.dashboard.packets.dashboard.PacketPlayerDisconnect;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.UUID;

/**
 * Created by Marc on 7/14/16
 */
public class Player {
    private UUID uuid;
    private String username;
    private Rank rank = Rank.GUEST;
    private String address;
    private String server;
    private UUID bungeeID;
    private boolean newGuest = false;
    private Timer tutorial = null;
    private boolean toggled = true;
    private boolean mentions = true;
    private long loginTime = System.currentTimeMillis();
    private UUID reply;
    private Mute mute;
    private HashMap<UUID, String> friends;
    private HashMap<UUID, String> requests;
    private boolean kicking;
    private int audioAuth = -1;
    private boolean recieveMessages = true;
    private String pack = "none";
    private String warp = "";
    private boolean pendingWarp = false;
    private boolean inventoryUploaded;
    private long onlineTime = 0;
    private String channel = "all";
    private long afkTime = System.currentTimeMillis();
    private boolean isAFK = false;

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
     * @param packet
     */
    public void send(BasePacket packet) {
        if (packet == null) {
            return;
        }
        Dashboard.getBungee(bungeeID).send(packet.getJSON().toString());
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return username;
    }

    public Rank getRank() {
        return rank;
    }

    public String getAddress() {
        return address;
    }

    public String getServer() {
        return server;
    }

    public UUID getBungeeID() {
        return bungeeID;
    }

    public boolean isToggled() {
        return toggled;
    }

    public boolean hasMentions() {
        return mentions;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setToggled(boolean b) {
        this.toggled = b;
    }

    public void setMentions(boolean b) {
        this.mentions = b;
    }

    public long getLoginTime() {
        return loginTime;
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

    public void setMute(Mute mute) {
        this.mute = mute;
    }

    public Mute getMute() {
        return mute;
    }

    public void setFriends(HashMap<UUID, String> friends) {
        this.friends = friends;
    }

    public void setRequests(HashMap<UUID, String> requests) {
        this.requests = requests;
    }

    public HashMap<UUID, String> getFriends() {
        return friends;
    }

    public HashMap<UUID, String> getRequests() {
        return requests;
    }

    public boolean hasFriendToggledOff() {
        return toggled;
    }

    public void setHasFriendToggled(boolean bool) {
        toggled = bool;
    }

    public boolean isKicking() {
        return kicking;
    }

    public int setAudioAuth() {
        this.audioAuth = new Random().nextInt(100000);
        return audioAuth;
    }

    public int getAudioAuth() {
        return audioAuth;
    }

    public void resetAudioAuth() {
        this.audioAuth = -1;
    }

    public boolean canRecieveMessages() {
        return recieveMessages;
    }

    public void setRecieveMessages(boolean recieveMessages) {
        this.recieveMessages = recieveMessages;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getPack() {
        return pack;
    }

    public void mention() {
        try {
            PacketMention packet = new PacketMention(uuid);
            Dashboard.getInstance(server).send(packet);
        } catch (Exception ignored) {
        }
    }

    public String getWarp() {
        return warp;
    }

    public void setWarp(String warp) {
        this.warp = warp;
    }

    public boolean isPendingWarp() {
        return pendingWarp;
    }

    public void setPendingWarp(boolean pendingWarp) {
        this.pendingWarp = pendingWarp;
    }

    public void setInventoryUploaded(boolean b) {
        this.inventoryUploaded = b;
    }

    public boolean isInventoryUploaded() {
        return inventoryUploaded;
    }

    public void setOnlineTime(long onlineTime) {
        this.onlineTime = onlineTime;
    }

    public long getOnlineTime() {
        return onlineTime;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isNewGuest() {
        return newGuest;
    }

    public void setNewGuest(boolean newGuest) {
        this.newGuest = newGuest;
    }

    public Timer getTutorial() {
        return tutorial;
    }

    public void setTutorial(Timer tutorial) {
        this.tutorial = tutorial;
    }

    public long getAfkTime() {
        return afkTime;
    }

    public void afkAction() {
        afkTime = System.currentTimeMillis();
    }

    public void setAFK(boolean AFK) {
        isAFK = AFK;
    }

    public boolean isAFK() {
        return isAFK;
    }
}