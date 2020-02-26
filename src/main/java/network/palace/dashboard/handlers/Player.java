package network.palace.dashboard.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.BaseComponent;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.chat.ComponentSerializer;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.bungee.PacketComponentMessage;
import network.palace.dashboard.packets.dashboard.*;
import network.palace.dashboard.server.DashboardSocketChannel;

import java.util.*;

/**
 * Created by Marc on 7/14/16
 */
public class Player {
    @Getter @Setter private UUID uuid;
    @Getter @Setter private String username;
    @Getter @Setter private Rank rank = Rank.SETTLER;
    private List<RankTag> tags = new ArrayList<>();
    @Getter private String address;
    @Getter @Setter private String server;
    @Getter @Setter private UUID bungeeID;
    @Getter private int mcVersion;
    @Getter @Setter private boolean newGuest = false;
    @Getter @Setter private Timer tutorial = null;
    @Getter @Setter private boolean friendRequestToggle = true;
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
    @Getter @Setter private long warningDelay = 0;
    private List<IgnoreData> ignoredUsers = new ArrayList<>();
    /**
     * Automatically send player's inventory to target server when switching to a server that handles inventories
     */
    @Getter @Setter private boolean sendInventoryOnJoin = false;
    @Getter @Setter private String isp = "unknown";

    public Player(UUID uuid, String username, String address, String server, UUID bungeeID, int mcVersion) {
        this.uuid = uuid;
        this.username = username;
        this.address = address;
        this.server = server;
        this.bungeeID = bungeeID;
        this.mcVersion = mcVersion;
    }

    public List<RankTag> getTags() {
        if (tags == null || tags.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(tags);
    }

    public void addTag(RankTag tag) {
        if (tag == null || tags.contains(tag)) return;
        tags.add(tag);
        tags.sort((rankTag, t1) -> t1.getId() - rankTag.getId());
    }

    public boolean removeTag(RankTag tag) {
        if (tag == null) return false;
        return tags.remove(tag);
    }

    public boolean hasTag(RankTag tag) {
        return tags.contains(tag);
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
        if (packet == null) return;
        DashboardSocketChannel bungee = Dashboard.getBungee(bungeeID);
        if (bungee == null) {
            Launcher.getDashboard().getLogger().info("CANCELLED PACKET EVENT INVALID BUNGEE '" + bungeeID + "' '" + uuid + "' '" + username + "'");
            return;
        }
        bungee.send(packet);
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
            return new Mute(uuid, username, false, System.currentTimeMillis(), System.currentTimeMillis(), "", "");
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

    public boolean hasMentions() {
        return mentions;
    }

    public void afkAction() {
        afkTime = System.currentTimeMillis();
    }

    public boolean hasFriendToggledOff() {
        return friendRequestToggle;
    }

    public boolean canRecieveMessages() {
        return receiveMessages;
    }

    public void setIgnoredUsers(List<IgnoreData> ignoredUsers) {
        this.ignoredUsers = ignoredUsers;
    }

    /**
     * Check if this player ignores the UUID of a player
     *
     * @param uuid the uuid to check
     * @return whether or not the player blocks that UUID
     */
    public boolean isIgnored(UUID uuid) {
        for (IgnoreData data : ignoredUsers) {
            if (data.getUuid().equals(getUniqueId()) && data.getIgnored().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public void ignorePlayer(UUID uuid) {
        Launcher.getDashboard().getMongoHandler().ignorePlayer(this, uuid);
    }

    public void unignorePlayer(UUID uuid) {
        for (IgnoreData data : ignoredUsers) {
            if (data.getUuid().equals(getUniqueId()) && data.getIgnored().equals(uuid)) {
                ignoredUsers.remove(data);
                break;
            }
        }
        Launcher.getDashboard().getMongoHandler().unignorePlayer(this, uuid);
    }

    public List<IgnoreData> getIgnoreData() {
        return new ArrayList<>(ignoredUsers);
    }

    public void addIgnoreData(IgnoreData data) {
        ignoredUsers.add(data);
    }

    public void runTutorial() {
        Timer tutorial = new Timer();
        tutorial.schedule(new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                switch (i) {
                    case 0: {
                        sendMessage(ChatColor.GREEN + "\nWelcome to the " + ChatColor.AQUA + "Palace Network" +
                                ChatColor.GREEN + ", we're happy you're here!");
                        mention();
                        break;
                    }
                    case 4: {
                        sendMessage(ChatColor.GREEN + "\nWe are an all-inclusive family-friendly " +
                                ChatColor.DARK_GREEN + "Minecraft " + ChatColor.GREEN + "gaming network!");
                        mention();
                        break;
                    }
                    case 7: {
                        sendMessage(ChatColor.GREEN + "\nRight now you're at the " + ChatColor.AQUA +
                                "Hub. " + ChatColor.GREEN + "From here, you can get to all of the different parts of our network.");
                        mention();
                        break;
                    }
                    case 15: {
                        sendMessage(ChatColor.GREEN + "\nArcade Games, Theme Parks, and a Creative server to name a few.");
                        mention();
                        break;
                    }
                    case 21: {
                        sendMessage(ChatColor.GREEN + "\nYou can also use your " + ChatColor.AQUA +
                                "Navigation Star " + ChatColor.GREEN + "to get to the different parts of our server.");
                        mention();
                        break;
                    }
                    case 28: {
                        sendMessage(ChatColor.GREEN + "\nInstall our Resource Pack for the " +
                                ChatColor.AQUA + "best " + ChatColor.GREEN +
                                "experience possible! All you have to do is type " + ChatColor.AQUA +
                                "/pack " + ChatColor.GREEN + "on a Park server and select " + ChatColor.DARK_GREEN +
                                "Yes. " + ChatColor.GRAY + "" + ChatColor.ITALIC +
                                "(You can set this up when the tutorial finishes)");
                        mention();
                        break;
                    }
                    case 36: {
                        sendMessage(ChatColor.GREEN + "\nAlso, connect to our " + ChatColor.BLUE +
                                "Audio Server " + ChatColor.GREEN + "for an immersive experience! You will hear the " +
                                ChatColor.AQUA + "sounds from rides, music from shows, and so much more! " +
                                ChatColor.GREEN + "Just type " + ChatColor.AQUA + "/audio " + ChatColor.GREEN +
                                "and click the message to connect. " + ChatColor.GRAY + "" + ChatColor.ITALIC +
                                "(You can set this up when the tutorial finishes)");
                        mention();
                        break;
                    }
                    case 49: {
                        sendMessage(ChatColor.GREEN + "\nBefore you start exploring, please take a " +
                                "few minutes to review our rules: " + ChatColor.AQUA +
                                "palace.network/rules " + ChatColor.GREEN + "\nWe are a " +
                                "family-friendly server with a goal of providing a safe, fun experience " +
                                "to all of our settlers.");
                        mention();
                        break;
                    }
                    case 58: {
                        sendMessage(ChatColor.GREEN + "\nAfter you finish reviewing our rules, " +
                                "you're finished with the tutorial! " + ChatColor.DARK_AQUA +
                                "Note: New settlers must wait " + ChatColor.BOLD + "10 minutes " +
                                ChatColor.DARK_AQUA + "before using chat. Read why: " +
                                ChatColor.AQUA + "palace.network/rules#chat");
                        mention();
                        setNewGuest(false);
                        Launcher.getDashboard().getMongoHandler().completeTutorial(getUniqueId());
                        cancel();
                    }
                }
                i++;
            }
        }, 2000, 1000);
        setTutorial(tutorial);
    }

    public void sendServerIgnoreList() {
        sendServerIgnoreList(server);
    }

    public void sendServerIgnoreList(String server) {
        List<String> ignoredList = new ArrayList<>();
        for (IgnoreData data : getIgnoreData()) {
            ignoredList.add(data.getIgnored().toString());
        }
        DashboardSocketChannel socket = Dashboard.getInstance(server);
        PacketIgnoreList packet = new PacketIgnoreList(getUniqueId(), ignoredList);
        socket.send(packet);
    }

    public void sendMessage(BaseComponent... baseComponents) {
        if (baseComponents == null) return;
        send(new PacketComponentMessage(
                Collections.singletonList(uuid),
                ComponentSerializer.toString(baseComponents)
        ));
    }
}