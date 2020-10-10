package network.palace.dashboard.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.discordSocket.DiscordCacheInfo;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketPlayerRank;
import network.palace.dashboard.packets.inventory.Resort;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;
import network.palace.dashboard.utils.IPUtil;
import network.palace.dashboard.utils.InventoryUtil;
import network.palace.dashboard.utils.MCLeakUtil;
import network.palace.dashboard.utils.NameUtil;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Innectic
 * @since 9/23/2017
 */
@SuppressWarnings("rawtypes")
public class MongoHandler {
    private MongoClient client = null;
    @Getter private MongoDatabase database = null;
    @Getter private MongoCollection<Document> playerCollection = null;
    @Getter private MongoCollection<Document> chatCollection = null;
    @Getter private MongoCollection<Document> activityCollection = null;
    @Getter private MongoCollection<Document> friendsCollection = null;
    @Getter private MongoCollection<Document> bansCollection = null;
    @Getter private MongoCollection<Document> permissionCollection = null;
    @Getter private MongoCollection<Document> resourcePackCollection = null;
    @Getter private MongoCollection<Document> honorMappingCollection = null;
    @Getter private MongoCollection<Document> outfitsCollection = null;
    @Getter private MongoCollection<Document> hotelCollection = null;
    @Getter private MongoCollection<Document> serversCollection = null;
    @Getter private MongoCollection<Document> staffLoginCollection = null;
    @Getter private MongoCollection<Document> votingCollection = null;
    @Getter private MongoCollection<Document> warpsCollection = null;
    @Getter private MongoCollection<Document> infractionsCollection = null;
    @Getter private MongoCollection<Document> storageCollection = null;
    @Getter private MongoCollection<Document> spamIpWhitelist = null;
    @Getter private MongoCollection<Document> helpRequestsCollection = null;

    public MongoHandler() throws IOException {
        String address = "";
        String database = "";
        String username = "";
        String password = "";
        try (BufferedReader br = new BufferedReader(new FileReader("db.txt"))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("address:")) {
                    address = line.split("address:")[1];
                }
                if (line.startsWith("username:")) {
                    username = line.split("username:")[1];
                }
                if (line.startsWith("password:")) {
                    password = line.split("password:")[1];
                }
                if (line.startsWith("database:")) {
                    database = line.split("database:")[1];
                }
                line = br.readLine();
            }
        }
        connect("mongodb://" + username + ":" + password + "@" + address + "/" + database);
    }

    public void connect(String uri) {
        MongoClientURI connectionString = new MongoClientURI(uri);
        client = new MongoClient(connectionString);
        database = client.getDatabase("palace");
        playerCollection = database.getCollection("players");
        chatCollection = database.getCollection("chat");
        activityCollection = database.getCollection("activity");
        friendsCollection = database.getCollection("friends");
        bansCollection = database.getCollection("bans");
        permissionCollection = database.getCollection("permissions");
        resourcePackCollection = database.getCollection("resourcepacks");
        honorMappingCollection = database.getCollection("honormapping");
        outfitsCollection = database.getCollection("outfits");
        hotelCollection = database.getCollection("hotels");
        serversCollection = database.getCollection("servers");
        staffLoginCollection = database.getCollection("stafflogin");
        votingCollection = database.getCollection("voting");
        warpsCollection = database.getCollection("warps");
        infractionsCollection = database.getCollection("infractions");
        storageCollection = database.getCollection("storage");
        spamIpWhitelist = database.getCollection("spamipwhitelist");
        helpRequestsCollection = database.getCollection("help_requests");
    }

    public void logInfraction(String name, String message) {
        infractionsCollection.insertOne(new Document("name", name).append("message", message));
    }

    public void createPlayer(Player player) {
        player.setNewGuest(true);
        player.setOnlineTime(1);

        Document playerDocument = new Document();
        playerDocument.put("uuid", player.getUniqueId().toString());
        playerDocument.put("username", player.getUsername());
        playerDocument.put("previousNames", new ArrayList<>());
        playerDocument.put("balance", 250);
        playerDocument.put("tokens", 0);
        playerDocument.put("server", player.getServer().isEmpty() ? "Unknown" : player.getServer());
        playerDocument.put("isp", "");
        playerDocument.put("country", "");
        playerDocument.put("region", "");
        playerDocument.put("regionName", "");
        playerDocument.put("timezone", "");
        playerDocument.put("lang", "en_US");
        playerDocument.put("minecraftVersion", player.getMcVersion());
        playerDocument.put("honor", 1);
        playerDocument.put("ip", player.getAddress());
        playerDocument.put("rank", player.getRank().getDBName());
        playerDocument.put("lastOnline", System.currentTimeMillis());
        playerDocument.put("onlineTime", 1L);

        Map<String, String> skinData = new HashMap<>();
        skinData.put("hash", "");
        skinData.put("signature", "");
        playerDocument.put("skin", skinData);

        List<Integer> cosmeticData = new ArrayList<>();
        playerDocument.put("cosmetics", cosmeticData);

        List<Object> kicks = new ArrayList<>();
        List<Object> mutes = new ArrayList<>();
        List<Object> bans = new ArrayList<>();
        playerDocument.put("kicks", kicks);
        playerDocument.put("mutes", mutes);
        playerDocument.put("bans", bans);

        Map<String, Object> parkData = new HashMap<>();
        List<Object> storageData = new ArrayList<>();
        parkData.put("storage", storageData);

        Map<String, String> magicBandData = new HashMap<>();
        magicBandData.put("bandtype", "blue");
        magicBandData.put("namecolor", "orange");
        parkData.put("magicband", magicBandData);

        Map<String, Integer> fpData = new HashMap<>();
        fpData.put("slow", 0);
        fpData.put("moderate", 0);
        fpData.put("thrill", 0);
        fpData.put("sday", 0);
        fpData.put("mday", 0);
        fpData.put("tday", 0);
        parkData.put("fastpass", fpData);

        List<Object> rideData = new ArrayList<>();
        parkData.put("rides", rideData);

        parkData.put("outfit", "0,0,0,0");
        List<Object> outfitData = new ArrayList<>();
        parkData.put("outfitPurchases", outfitData);

        Map<String, Object> parkSettings = new HashMap<>();
        parkSettings.put("visibility", true);
        parkSettings.put("flash", true);
        parkSettings.put("hotel", true);
        parkSettings.put("pack", "");
        parkData.put("settings", parkSettings);

        playerDocument.put("parks", parkData);

        Map<String, Object> creativeData = new HashMap<>();
        creativeData.put("particle", "none");
        creativeData.put("rptag", false);
        creativeData.put("rplimit", 5);
        creativeData.put("showcreator", false);
        creativeData.put("creator", false);
        creativeData.put("creatortag", false);
        creativeData.put("resourcepack", "none");

        playerDocument.put("creative", creativeData);

        Map<String, Object> voteData = new HashMap<>();
        voteData.put("lastTime", 0L);
        voteData.put("lastSite", 0);
        playerDocument.put("vote", voteData);

        Map<String, Long> monthlyRewards = new HashMap<>();
        monthlyRewards.put("settler", 0L);
        playerDocument.put("monthlyRewards", monthlyRewards);

        playerDocument.put("tutorial", false);

        Map<String, Object> settings = new HashMap<>();
        settings.put("mentions", true);
        settings.put("friendRequestToggle", true);
        playerDocument.put("settings", settings);

        List<Object> achievements = new ArrayList<>();
        playerDocument.put("achievements", achievements);

        List<Object> autographs = new ArrayList<>();
        playerDocument.put("autographs", autographs);

        List<Object> transactions = new ArrayList<>();
        playerDocument.put("transactions", transactions);

        List<Object> ignoring = new ArrayList<>();
        playerDocument.put("ignoring", ignoring);

        playerCollection.insertOne(playerDocument);

        Dashboard dashboard = Launcher.getDashboard();

        dashboard.addPlayer(player);

        updatePreviousUsernames(player.getUniqueId(), player.getUsername());
    }

    public Document getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    /**
     * Get a player's full document from the database
     *
     * @param uuid the uuid
     * @return the <b>full</b> document
     * @implNote This method shouldn't be used frequently, use {@link #getPlayer(UUID, Document)} to get specific data
     */
    public Document getPlayer(UUID uuid) {
        return playerCollection.find(MongoFilter.UUID.getFilter(uuid.toString())).first();
    }

    /**
     * Get a specific set of a player's data from the database
     *
     * @param uuid  the uuid
     * @param limit a Document specifying which keys to return from the database
     * @return a Document with the limited data
     */
    public Document getPlayer(UUID uuid, Document limit) {
        FindIterable<Document> doc = playerCollection.find(MongoFilter.UUID.getFilter(uuid.toString())).projection(limit);
        if (doc == null) return null;
        return doc.first();
    }

    public Document getPlayer(String player) {
        return playerCollection.find(MongoFilter.USERNAME.getFilter(player)).first();
    }

    public Document getPlayer(String player, Document limit) {
        FindIterable<Document> doc = playerCollection.find(MongoFilter.USERNAME.getFilter(player)).projection(limit);
        if (doc == null) return null;
        return doc.first();
    }

    /**
     * Set previous usernames for a player
     *
     * @param uuid the uuid
     * @param list the list of previous usernames
     */
    public void setPreviousNames(UUID uuid, List<String> list) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("previousNames", list));
    }

    public void updatePreviousUsernames(UUID uuid, String username) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            String current = "";
            try {
                List<String> list = NameUtil.getNames(username, uuid.toString().replaceAll("-", ""));
                Collections.reverse(list);
                current = list.get(0);
                setPreviousNames(uuid, list.subList(1, list.size()));
            } catch (Exception e) {
                Launcher.getDashboard().getLogger().error("Error retrieving previous usernames", e);
            }
            if (!username.isEmpty() && !current.equals(username)) {
                playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("username", current));
            }
        });
    }

    public boolean isPlayerMuted(UUID uuid) {
        Mute m = getCurrentMute(uuid);
        return m != null && m.isMuted();
    }

    public boolean isPlayerBanned(UUID uuid) {
        return getCurrentBan(uuid) != null;
    }

    public Mute getCurrentMute(UUID uuid) {
        return getCurrentMute(uuid, "");
    }

    public Mute getCurrentMute(UUID uuid, String name) {
        Document doc = getPlayer(uuid, new Document("mutes", 1));
        for (Object o : doc.get("mutes", ArrayList.class)) {
            Document muteDoc = (Document) o;
            if (muteDoc == null || !muteDoc.getBoolean("active")) continue;
            return new Mute(uuid, name, muteDoc);
        }
        return new Mute(uuid, name, null);
    }

    public void unmutePlayer(UUID uuid) {
        playerCollection.updateMany(new Document("uuid", uuid.toString()).append("mutes.active", true), Updates.set("mutes.$.active", false));
    }

    public void mutePlayer(UUID uuid, Mute mute) {
        if (isPlayerMuted(uuid)) return;

        Document muteDocument = new Document("created", mute.getCreated()).append("expires", mute.getExpires())
                .append("reason", mute.getReason()).append("source", mute.getSource()).append("active", true);

        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.push("mutes", muteDocument));
    }

    public Ban getCurrentBan(UUID uuid) {
        return getCurrentBan(uuid, "");
    }

    public Ban getCurrentBan(UUID uuid, String name) {
        Document doc = getPlayer(uuid, new Document("bans", 1));
        for (Object o : doc.get("bans", ArrayList.class)) {
            Document banDoc = (Document) o;
            if (banDoc == null || !banDoc.getBoolean("active")) continue;
            return new Ban(uuid, name, banDoc);
        }
        return null;
    }

    public void kickPlayer(UUID uuid, Kick kick) {
        Document kickDocument = new Document("reason", kick.getReason())
                .append("time", System.currentTimeMillis())
                .append("source", kick.getSource());
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.push("kicks", kickDocument));
    }

    public void warnPlayer(Warning warning) {
        Document warningDocument = new Document("reason", warning.getReason())
                .append("time", System.currentTimeMillis())
                .append("source", warning.getSource());
        playerCollection.updateOne(MongoFilter.UUID.getFilter(warning.getUniqueId().toString()),
                Updates.push("warnings", warningDocument), new UpdateOptions().upsert(true));
    }

    public void unbanPlayer(UUID uuid) {
        playerCollection.updateMany(new Document("uuid", uuid.toString()).append("bans.active", true),
                new Document("$set", new Document("bans.$.active", false).append("bans.$.expires", System.currentTimeMillis())));
    }

    public void banPlayer(UUID uuid, Ban ban) {
        if (isPlayerBanned(uuid)) return;

        Document banDocument = new Document("created", ban.getCreated()).append("expires", ban.getExpires())
                .append("permanent", ban.isPermanent()).append("reason", ban.getReason())
                .append("source", ban.getSource()).append("active", true);

        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.push("bans", banDocument));
    }

    public void banProvider(ProviderBan ban) {
        bansCollection.insertOne(new Document("type", "provider").append("data", ban.getProvider())
                .append("source", ban.getSource()));
    }

    public ProviderBan getProviderBan(String isp) {
        Document doc = bansCollection.find(new Document("type", "provider").append("data", isp)).first();
        if (doc == null) return null;
        return new ProviderBan(doc.getString("data"), doc.getString("source"));
    }

    public List<String> getBannedProviders() {
        List<String> list = new ArrayList<>();
        for (Document doc : bansCollection.find(new Document("type", "provider"))) {
            list.add(doc.getString("data"));
        }
        return list;
    }

    public void unbanProvider(String isp) {
        bansCollection.deleteMany(new Document("type", "provider").append("data", isp));
    }

    public void banAddress(AddressBan ban) {
        bansCollection.insertOne(new Document("type", "ip").append("data", ban.getAddress())
                .append("reason", ban.getReason()).append("source", ban.getSource()));
    }

    public AddressBan getAddressBan(String address) {
        Document doc = bansCollection.find(new Document("type", "ip").append("data", address)).first();
        if (doc == null) return null;
        return new AddressBan(doc.getString("data"), doc.getString("reason"), doc.getString("source"));
    }

    public void unbanAddress(String address) {
        bansCollection.deleteMany(new Document("type", "ip").append("data", address));
    }

    public void updateProviderData(UUID uuid, IPUtil.ProviderData data) {
        Document doc = new Document("isp", data.getIsp()).append("country", data.getCountry())
                .append("region", data.getRegion()).append("regionName", data.getRegionName())
                .append("timezone", data.getTimezone());
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), new Document("$set", doc));
    }

    public void login(Player player) {
        login(player, false);
    }

    /**
     * Initial login method where all player data is collected from the database and stored properly
     *
     * @param player       The player object
     * @param afterRestart true if processing after Dashboard restart where no login messages should be sent, false if normal login
     */
    @SuppressWarnings("rawtypes")
    public void login(Player player, boolean afterRestart) {
        Dashboard dashboard = Launcher.getDashboard();

        if (!afterRestart) {
            dashboard.getSchedulerManager().runAsync(() -> {
                // Check if the uuid is from MCLeaks before we continue.
                boolean isMCLeaks = MCLeakUtil.checkPlayer(player);
                if (isMCLeaks) {
                    // UUID is in MCLeaks, temp ban the account for 3 days
                    Ban tempBan = new Ban(player.getUniqueId(), player.getUsername(), false, System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000), "MCLeaks Account", "Dashboard");
                    banPlayer(player.getUniqueId(), tempBan);
                    dashboard.getModerationUtil().announceBan(tempBan);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            player.kickPlayer(ChatColor.RED + "MCLeaks Accounts are not allowed on Palace Network\n" +
                                    ChatColor.AQUA + "If you think you were banned incorrectly, submit an appeal at palnet.us/appeal");
                        }
                    }, 1000);
                }
            });
        }

        dashboard.getSchedulerManager().runAsync(() -> {
            try {
                Document doc = getPlayer(player.getUniqueId(), new Document("rank", 1).append("tags", 1)
                        .append("ip", 1).append("username", 1).append("friendRequestToggle", 1).append("onlineTime", 1)
                        .append("tutorial", 1).append("minecraftVersion", 1).append("settings", 1));
                if (doc == null) {
                    createPlayer(player);
                    return;
                }
                long ot = doc.getLong("onlineTime");
                player.setOnlineTime(ot == 0 ? 1 : ot);

                Rank rank = Rank.fromString(doc.getString("rank"));
                if (doc.get("tags") != null) {
                    ArrayList tags = doc.get("tags", ArrayList.class);
                    for (Object s : tags) {
                        RankTag tag = RankTag.fromString((String) s);
                        if (tag != null) {
                            player.addTag(tag);
                        }
                    }
                }

                if (!rank.equals(Rank.SETTLER) || !player.getTags().isEmpty()) {
                    List<String> tags = new ArrayList<>();
                    player.getTags().forEach(t -> tags.add(t.getDBName()));
                    PacketPlayerRank packet = new PacketPlayerRank(player.getUniqueId(), rank, tags);
                    player.send(packet);
                }

                if (dashboard.isStrictMode() && rank.getRankId() >= Rank.TRAINEE.getRankId())
                    player.sendMessage(ChatColor.RED + "Chat is currently in strict mode!");

                String ip = doc.getString("ip");
                int protocolVersion = doc.getInteger("minecraftVersion");
                String username = doc.getString("username");

                boolean disable = !player.getAddress().equals(doc.getString("ip")) && rank.getRankId() >= Rank.TRAINEE.getRankId();

                if (!disable && (!ip.equals(player.getAddress()) || protocolVersion != player.getMcVersion() ||
                        !username.equals(player.getUsername()))) {
                    playerCollection.updateOne(MongoFilter.UUID.getFilter(player.getUniqueId().toString()),
                            new Document("$set", new Document("ip", player.getAddress())
                                    .append("username", player.getUsername())
                                    .append("minecraftVersion", new BsonInt32(player.getMcVersion()))));
                    if (!username.equals(player.getUsername())) {
                        int member_id = getForumMemberId(player.getUniqueId());
                        if (member_id != -1) {
                            dashboard.getForum().updatePlayerName(player.getUniqueId(), member_id, player.getUsername());
                        }
                        updatePreviousUsernames(player.getUniqueId(), player.getUsername());
                    }
                }
                Document settings = (Document) doc.get("settings");

                player.setDisabled(disable);
                player.setRank(rank);
                player.setFriendRequestToggle(!settings.getBoolean("friendRequestToggle"));
                player.setMentions(settings.getBoolean("mentions"));
                player.setNewGuest(!doc.getBoolean("tutorial"));
                if (!afterRestart) dashboard.addPlayer(player);
                dashboard.getLogger().info("Player Join: " + player.getUsername() + "|" + player.getUniqueId());
                dashboard.addToCache(player.getUniqueId(), player.getUsername());

                if (!afterRestart && rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                            rank.getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked in.";
                    if (disable) {
                        msg += ChatColor.GRAY + " (not logged in)";
                    }
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                            tp.sendMessage(msg);
                        }
                    }
                    staffClock(player.getUniqueId(), true);
                    if (rank.getRankId() >= Rank.TRAINEE.getRankId() && dashboard.getChatUtil().isChatMuted("ParkChat")) {
                        player.sendMessage(ChatColor.RED + "\n\n\nChat is currently muted!\n\n\n");
                    }
                }

                List<IgnoreData> ignored = getIgnoreData(player);
                player.setIgnoredUsers(ignored);

                HashMap<UUID, String> friends = getFriendList(player.getUniqueId());
                HashMap<UUID, String> requests = getRequestList(player.getUniqueId());
                player.setFriends(friends);
                player.setRequests(requests);
                if (requests.size() > 0) {
                    player.sendMessage(ChatColor.AQUA + "You have " + ChatColor.YELLOW + "" + ChatColor.BOLD +
                            requests.size() + " " + ChatColor.AQUA +
                            "pending friend request" + (requests.size() > 1 ? "s" : "") + "! View them with " +
                            ChatColor.YELLOW + ChatColor.BOLD + "/friend requests");
                }
                HashMap<UUID, String> friendList = player.getFriends();
                if (friendList != null && !afterRestart) {
                    String joinMessage = rank.getTagColor() + player.getUsername() + ChatColor.LIGHT_PURPLE + " has joined.";
                    dashboard.getFriendUtil().friendMessage(player, friendList, joinMessage);
                }
                Mute mute = getCurrentMute(player.getUniqueId(), player.getUsername());
                player.setMute(mute);
                if (disable) {
                    SlackMessage m = new SlackMessage("");
                    SlackAttachment a = new SlackAttachment("*" + rank.getName() + "* `" + player.getUsername() +
                            "` connected from a new IP address `" + player.getAddress() + "`");
                    a.color("warning");
                    dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a), false);
                    player.sendMessage(ChatColor.YELLOW + "\n\n" + ChatColor.BOLD +
                            "You connected with a new IP address, type " + ChatColor.GREEN + "" + ChatColor.BOLD +
                            "/staff login [password]" + ChatColor.YELLOW + "" + ChatColor.BOLD + " to verify your account.\n");
                }
            } catch (Exception e) {
                Launcher.getDashboard().getLogger().error("Error handling player login", e);
            }
        });
    }

    public void logout(Player player) {
        Dashboard dashboard = Launcher.getDashboard();
        String server = "Unknown";
        if (player.getServer() != null) {
            server = player.getServer();
        }
        playerCollection.updateOne(MongoFilter.UUID.getFilter(player.getUniqueId().toString()),
                new Document("$set", new Document("server", server).append("lastOnline", System.currentTimeMillis()))
                        .append("$inc", new Document("onlineTime", (int) ((System.currentTimeMillis() / 1000) -
                                (player.getLoginTime() / 1000)))));
        Rank rank = player.getRank();
        if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
            String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                    rank.getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked out.";
            for (Player tp : dashboard.getOnlinePlayers()) {
                if (tp.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                    tp.sendMessage(msg);
                }
            }
            staffClock(player.getUniqueId(), false);
        }
        HashMap<UUID, String> flist = player.getFriends();
        if (!flist.isEmpty()) {
            String msg = rank.getTagColor() + player.getUsername() + ChatColor.LIGHT_PURPLE + " has left.";
            dashboard.getFriendUtil().friendMessage(player, flist, msg);
        }

    }

    private List<IgnoreData> getIgnoreData(Player player) {
        List<IgnoreData> list = new ArrayList<>();
        for (Object o : getPlayer(player.getUniqueId(), new Document("ignoring", 1)).get("ignoring", ArrayList.class)) {
            Document doc = (Document) o;
            list.add(new IgnoreData(player.getUniqueId(), UUID.fromString(doc.getString("uuid")), doc.getLong("started")));
        }
        return list;
    }

    /**
     * Get UUID from player's username
     *
     * @param username the username
     * @return their UUID or null if isn't formatted like UUID
     */
    public UUID usernameToUUID(String username) {
        try {
            FindIterable<Document> list = playerCollection.find(MongoFilter.USERNAME.getFilter(username));
            if (list == null || list.first() == null) return null;
            return UUID.fromString(list.first().getString("uuid"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get username from player's UUID
     *
     * @param uuid the username
     * @return their username
     */
    public String uuidToUsername(UUID uuid) {
        try {
            FindIterable<Document> list = playerCollection.find(MongoFilter.UUID.getFilter(uuid.toString()));
            if (list == null || list.first() == null) return null;
            return list.first().getString("username");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public List<String> getPlayersOnIP(String ip) {
        List<String> players = new ArrayList<>();

        playerCollection.find(Filters.eq("ip", ip)).projection(new Document("username", 1))
                .forEach((Consumer<Document>) document -> players.add(document.getString("username")));
        return players;
    }

    public List<UUID> getPlayersByRank(Rank... ranks) {
        List<UUID> foundPlayers = new ArrayList<>();
        for (Rank rank : ranks) {
            playerCollection.find(Filters.eq("rank", rank.getDBName())).forEach((Consumer<Document>) document ->
                    foundPlayers.add(UUID.fromString(document.getString("uuid"))));
        }
        return foundPlayers;
    }

    public List<String> getPlayerNamesFromRank(Rank rank) {
        List<String> list = new ArrayList<>();
        playerCollection.find(MongoFilter.RANK.getFilter(rank.getDBName())).projection(new Document("username", 1))
                .forEach((Consumer<Document>) d -> list.add(d.getString("username")));
        return list;
    }

    public List<UUID> getPlayerUUIDsFromRank(Rank rank) {
        List<UUID> list = new ArrayList<>();
        playerCollection.find(MongoFilter.RANK.getFilter(rank.getDBName())).projection(new Document("uuid", 1))
                .forEach((Consumer<Document>) d -> list.add(UUID.fromString(d.getString("uuid"))));
        return list;
    }

    /**
     * Get rank from uuid
     *
     * @param uuid the uuid
     * @return the rank, or settler if doesn't exist
     */
    public Rank getRank(UUID uuid) {
        if (uuid == null) return Rank.SETTLER;
        Document result = playerCollection.find(MongoFilter.UUID.getFilter(uuid.toString())).projection(new Document("rank", 1)).first();
        if (result == null || !result.containsKey("rank")) return Rank.SETTLER;
        return Rank.fromString(result.getString("rank"));
    }

    @SuppressWarnings("rawtypes")
    public List<RankTag> getRankTags(UUID uuid) {
        if (uuid == null) return new ArrayList<>();
        Document result = playerCollection.find(MongoFilter.UUID.getFilter(uuid.toString())).projection(new Document("tags", 1)).first();
        if (result == null || !result.containsKey("tags")) return new ArrayList<>();
        ArrayList list = result.get("tags", ArrayList.class);
        List<RankTag> tags = new ArrayList<>();
        for (Object o : list) {
            tags.add(RankTag.fromString((String) o));
        }
        return tags;
    }

    public void addRankTag(UUID uuid, RankTag tag) {
        if (uuid == null || tag == null) return;
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.addToSet("tags", tag.getDBName()), new UpdateOptions().upsert(true));
    }

    public void removeRankTag(UUID uuid, RankTag tag) {
        if (uuid == null || tag == null) return;
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.pull("tags", tag.getDBName()));
    }

    /**
     * Log an accepted help request in the database
     *
     * @param requesting the requesting player
     * @param helping    the helping staff member
     */
    public void logHelpRequest(UUID requesting, UUID helping) {
        helpRequestsCollection.insertOne(new Document("requesting", requesting.toString())
                .append("helping", helping.toString()).append("time", System.currentTimeMillis()));
    }

    /**
     * Get the staff member's activity accepting help request
     *
     * @param staffMember the staff member to look up
     * @return a String with comma-separated values for accepted help requests: last day, last week, last month, all time
     */
    public String getHelpActivity(UUID staffMember) {
        List<Long> requests = new ArrayList<>();
        for (Document doc : helpRequestsCollection.find(Filters.eq("helping", staffMember.toString())).projection(new Document("time", true))) {
            if (doc.containsKey("time")) requests.add(doc.getLong("time"));
        }
        requests.sort((o1, o2) -> (int) (o1 - o2));
        long dayAgo = Instant.now().minus(Duration.ofDays(1)).toEpochMilli();
        long weekAgo = Instant.now().minus(Duration.ofDays(7)).toEpochMilli();
        long monthAgo = Instant.now().minus(Duration.ofDays(30)).toEpochMilli();
        int dayTotal = 0, weekTotal = 0, monthTotal = 0, total = 0;
        for (long r : requests) {
            if (r >= dayAgo) dayTotal++;
            if (r >= weekAgo) weekTotal++;
            if (r >= monthAgo) monthTotal++;
            total++;
        }
        return dayTotal + "," + weekTotal + "," + monthTotal + "," + total;
    }

    /**
     * Log activity to the collection
     *
     * @param uuid        the uuid
     * @param action      the action
     * @param description the description
     */
    public void logActivity(UUID uuid, String action, String description) {
        activityCollection.insertOne(new Document("uuid", uuid).append("action", action).append("description", description));
    }

    /**
     * Log a transaction in the database
     *
     * @param uuid   the uuid
     * @param amount the amount
     * @param source the source of the transaction
     * @param type   the currency type
     * @param set    whether or not the transaction was a set
     */
    public void logTransaction(UUID uuid, int amount, String source, CurrencyType type, boolean set) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.push("transactions", new BasicDBObject("amount", amount)
                .append("type", (set ? "set " : "add ") + type.getName())
                .append("source", source)
                .append("server", "Dashboard")
                .append("timestamp", System.currentTimeMillis() / 1000)));
    }

    public BseenData getBseenInformation(UUID uuid) {
        Document player = getPlayer(uuid, new Document("username", 1).append("rank", 1).append("tags", 1)
                .append("lastOnline", 1).append("ip", 1).append("mutes", 1).append("server", 1));
        if (player == null) return null;
        Rank rank = Rank.fromString(player.getString("rank"));
        List<RankTag> tags = new ArrayList<>();
        if (player.containsKey("tags")) {
            for (Object o : player.get("tags", ArrayList.class)) {
                tags.add(RankTag.fromString((String) o));
            }
        }
        long lastLogin = player.getLong("lastOnline");
        String ipAddress = player.getString("ip");
        Mute mute = getCurrentMute(uuid);
        String server = player.getString("server");
        return new BseenData(uuid, rank, tags, lastLogin, ipAddress, mute, server);
    }

    public void staffClock(UUID uuid, boolean b) {
        staffLoginCollection.insertOne(new Document("uuid", uuid.toString()).append("time", System.currentTimeMillis()).append("login", b));
    }

    public void ignorePlayer(Player player, UUID uuid) {
        long time = System.currentTimeMillis();
        playerCollection.updateOne(MongoFilter.UUID.getFilter(player.getUniqueId().toString()), Updates.push("ignoring", new Document("uuid", uuid.toString()).append("started", time)));
        player.addIgnoreData(new IgnoreData(player.getUniqueId(), uuid, time));
    }

    public void unignorePlayer(Player player, UUID uuid) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(player.getUniqueId().toString()), Updates.pull("ignoring", new Document("uuid", uuid.toString())));
    }

    public void completeTutorial(UUID uuid) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("tutorial", true));
    }

    public List<Server> getServers() {
        return getServers(false);
    }

    public List<Server> getServers(boolean playground) {
        List<Server> list = new ArrayList<>();
        for (Document doc : serversCollection.find()) {
            if (playground) {
                if (!doc.containsKey("playground") || !doc.getBoolean("playground")) continue;
            } else {
                if (doc.containsKey("playground") && doc.getBoolean("playground")) continue;
            }
            list.add(new Server(doc.getString("name"), doc.getString("address"),
                    doc.getBoolean("park"), 0, doc.getString("type")));
        }
        return list;
    }

    public HashMap<UUID, String> getFriendList(UUID uuid) {
        return getList(uuid, 1);
    }

    public HashMap<UUID, String> getRequestList(UUID uuid) {
        return getList(uuid, 0);
    }

    public HashMap<UUID, String> getList(UUID uuid, int id) {
        Dashboard dashboard = Launcher.getDashboard();
        List<UUID> list = new ArrayList<>();
        for (Document doc : friendsCollection.find(Filters.or(Filters.eq("sender", uuid.toString()),
                Filters.eq("receiver", uuid.toString())))) {
            UUID sender = UUID.fromString(doc.getString("sender"));
            UUID receiver = UUID.fromString(doc.getString("receiver"));
            boolean friend = doc.getLong("started") > 0;
            if (id == 0 && !friend && receiver.equals(uuid)) {
                list.add(sender);
            } else if (id == 1 && friend) {
                if (uuid.equals(sender)) {
                    list.add(receiver);
                } else {
                    list.add(sender);
                }
            }
        }
        HashMap<UUID, String> map = new HashMap<>();
        for (UUID uid : list) {
            String username = dashboard.getCachedName(uid);
            if (username == null) {
                username = uuidToUsername(uid);
                dashboard.addToCache(uid, username);
            }
            map.put(uid, username);
        }
        return map;
    }

    public void addFriendRequest(UUID sender, UUID receiver) {
        friendsCollection.insertOne(new Document("sender", sender.toString()).append("receiver", receiver.toString())
                .append("started", 0L));
    }

    public void removeFriend(UUID sender, UUID receiver) {
        friendsCollection.deleteOne(Filters.or(
                new Document("sender", sender.toString()).append("receiver", receiver.toString()),
                new Document("receiver", sender.toString()).append("sender", receiver.toString())
        ));
    }

    /*
    Lego: 9ab3b4c4-71d8-47c9-9e7d-adf040c53d2b
    Jump: 9be1aee3-6b62-40ad-a7df-ead6314f8bd5
     */

    public void acceptFriendRequest(UUID receiver, UUID sender) {
        friendsCollection.updateOne(new Document("sender", sender.toString()).append("receiver", receiver.toString()),
                Updates.set("started", System.currentTimeMillis()));
    }

    public void denyFriendRequest(UUID receiver, UUID sender) {
        friendsCollection.deleteOne(new Document("sender", sender.toString()).append("receiver", receiver.toString()).append("started", 0L));
    }

    public boolean getFriendRequestToggle(UUID uuid) {
        Document doc = getPlayer(uuid, new Document("settings", 1));
        if (doc == null) {
            return false;
        }
        Document settings = (Document) doc.get("settings");
        if (settings == null) {
            return false;
        }
        return settings.getBoolean("friendRequestToggle");
    }

    public void setFriendRequestToggle(UUID uuid, boolean value) {
        setSetting(uuid, "friendRequestToggle", value);
    }

    public void disconnect() {
        client.close();
    }

    public void logChat(ChatMessage msg) {
        chatCollection.insertOne(new Document("uuid", msg.getUuid().toString()).append("message", msg.getMessage()).append("time", msg.getTime()));

        /*if (list.isEmpty()) return;
        List<Document> documents = new ArrayList<>();
        for (String s : list) {
            documents.add(new Document("uuid", uuid.toString()).append("message", s).append("time", System.currentTimeMillis() / 1000));
        }

        BsonArray array = new BsonArray();
        while (!list.isEmpty()) {
            array.add(new BsonDocument("message", new BsonString(list.remove(0))).append("time", new BsonInt64(System.currentTimeMillis() / 1000)));
        }
        chatCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.pushEach("messages", array),
                new UpdateOptions().upsert(true));*/
    }

    public void logAFK(UUID uuid) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()),
                Updates.push("afklogs", System.currentTimeMillis()), new UpdateOptions().upsert(true));
    }

    /*
    Park Methods
     */

    /**
     * Get data for a specific section of park data. If no limit is provided, the entire parks section is returned.
     *
     * @param uuid  the uuid of the player
     * @param limit a string specifying the limits of the search
     * @return a document with the requested data
     */
    public Document getParkData(UUID uuid, String limit) {
        if (limit == null || limit.isEmpty()) {
            return (Document) getPlayer(uuid, new Document("parks", 1)).get("parks");
        }
        Document current = (Document) getPlayer(uuid, new Document("parks." + limit, 1)).get("parks");
        String[] split;
        if (limit.contains(".")) {
            split = limit.split("\\.");
        } else {
            split = new String[]{limit};
        }
        for (String s : split) {
            current = (Document) current.get(s);
        }
        return current;
    }

    public Document getParkInventoryData(UUID uuid) {
        return storageCollection.find(MongoFilter.UUID.getFilter(uuid.toString())).first();
    }

    public Document getParkInventory(UUID uuid, Resort resort) {
        Document doc = getParkInventoryData(uuid);
        if (doc.containsKey(resort.getName())) return (Document) doc.get(resort.getName());
        doc = null;
        for (Object o : getParkInventoryData(uuid).get("storage", ArrayList.class)) {
            Document inv = (Document) o;
            if (inv.getInteger("resort") == resort.getId()) {
                doc = inv;
                break;
            }
        }
        return doc;
    }

    /**
     * Update a player's stored inventory
     *
     * @param uuid the uuid
     * @param inv  the inventory data
     */
    public void setInventoryData(UUID uuid, ResortInventory inv) {
        try {
            UpdateData data = InventoryUtil.getDataFromJson(inv.getBackpackJSON(), inv.getBackpackSize(),
                    inv.getLockerJSON(), inv.getLockerSize(), inv.getBaseJSON(), inv.getBuildJSON());
            if (storageCollection.find(Filters.eq("uuid", uuid.toString())).first() != null) {
                // Player already has a document, update the existing one
                setInventoryData(uuid, inv.getResort(), data);
            } else {
                // Player doesn't have a document, make a new one
                Document invDoc = new Document("backpack", data.getPack()).append("backpacksize", data.getPackSize())
                        .append("locker", data.getLocker()).append("lockersize", data.getLockerSize())
                        .append("base", data.getBase()).append("build", data.getBuild())
                        .append("version", InventoryUtil.STORAGE_VERSION)
                        .append("last-updated", System.currentTimeMillis());
                storageCollection.insertOne(new Document("uuid", uuid.toString()).append(inv.getResort().getName(), invDoc));
            }
        } catch (Exception e) {
            Launcher.getDashboard().getLogger().error("Error updating inventory data in the database", e);
        }
    }

    public void setInventoryData(UUID uuid, Resort resort, UpdateData data) {
        Document doc = new Document("backpack", data.getPack()).append("backpacksize", data.getPackSize())
                .append("locker", data.getLocker()).append("lockersize", data.getLockerSize())
                .append("base", data.getBase()).append("build", data.getBuild())
                .append("version", InventoryUtil.STORAGE_VERSION)
                .append("last-updated", System.currentTimeMillis());

        storageCollection.updateOne(Filters.eq("uuid", uuid.toString()),
                Updates.set(resort.getName(), doc), new UpdateOptions().upsert(true));
    }

    public void updateInventoryData(UUID uuid, InventoryUpdate update) {
        HashMap<Resort, UpdateData> map = update.getMap();
        for (Map.Entry<Resort, UpdateData> entry : map.entrySet()) {
            Resort resort = entry.getKey();
            UpdateData data = entry.getValue();
            setInventoryData(uuid, resort, data);
        }
    }

    public Document getSettings(UUID uuid) {
        return getPlayer(uuid, new Document("settings", 1));
    }

    public void setSetting(UUID uuid, String key, Object value) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("settings." + key, value),
                new UpdateOptions().upsert(true));
    }

    public void updateAddress(UUID uuid, String address) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("ip", address));
    }

    public boolean verifyPassword(UUID uuid, String pass) {
        Document doc = getPlayer(uuid, new Document("staff", 1).append("staffPassword", 1));
        if (doc == null || !doc.containsKey("staffPassword")) return false;
        String dbPassword = doc.getString("staffPassword");
        return Launcher.getDashboard().getPasswordUtil().validPassword(pass, dbPassword);
    }

    public boolean hasPassword(UUID uuid) {
        return getPlayer(uuid, new Document("staffPassword", 1)).containsKey("staffPassword");
    }

    public void setPassword(UUID uuid, String pass) {
        Dashboard dashboard = Launcher.getDashboard();
        String salt = dashboard.getPasswordUtil().getNewSalt();
        String hashed = dashboard.getPasswordUtil().hashPassword(pass, salt);
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("staffPassword", hashed), new UpdateOptions().upsert(true));
    }

    public ArrayList getBans(UUID uuid) {
        return getPlayer(uuid, new Document("bans", 1)).get("bans", ArrayList.class);
    }

    public ArrayList getMutes(UUID uuid) {
        return getPlayer(uuid, new Document("mutes", 1)).get("mutes", ArrayList.class);
    }

    public ArrayList getKicks(UUID uuid) {
        return getPlayer(uuid, new Document("kicks", 1)).get("kicks", ArrayList.class);
    }

    public ArrayList getWarnings(UUID uuid) {
        Document doc = getPlayer(uuid, new Document("warnings", 1));
        if (doc == null || !doc.containsKey("warnings")) {
            return new ArrayList();
        }
        return doc.get("warnings", ArrayList.class);
    }

    public void addServer(Server server) {
        Document serverDocument = new Document("name", server.getName()).append("type", server.getServerType())
                .append("address", server.getAddress()).append("park", server.isPark());
        if (Launcher.getDashboard().isTestNetwork()) {
            serverDocument.append("playground", true);
        }
        serversCollection.insertOne(serverDocument);
    }

    public void removeServer(String name) {
        serversCollection.deleteMany(new Document("name", name));
    }

    public void insertDiscord(final DiscordCacheInfo info) {
        SocketConnection.sendNewlink(info);
        playerCollection.updateOne(MongoFilter.UUID.getFilter(info.getMinecraft().getUuid()),
                Updates.set("discordUsername", info.getDiscord().getUsername()));
    }

    public void removeDiscord(DiscordCacheInfo info) {
        String discordUsername = info.getDiscord().getUsername();
        playerCollection.updateMany(Filters.or(MongoFilter.UUID.getFilter(info.getMinecraft().getUuid()),
                new Document("discordUsername", discordUsername)), Updates.unset("discordUsername"));
    }

    public DiscordCacheInfo getUserFromPlayer(Player player) {
        DiscordCacheInfo.Minecraft mc = new DiscordCacheInfo.Minecraft(player.getUsername(),
                player.getUniqueId().toString(), player.getRank().getDBName());
        Document doc = getPlayer(player.getUniqueId(), new Document("discordUsername", 1));
        if (doc == null || !doc.containsKey("discordUsername")) return null;
        return new DiscordCacheInfo(mc, new DiscordCacheInfo.Discord(doc.getString("discordUsername")));
    }

    public void addSpamIPWhitelist(SpamIPWhitelist whitelist) {
        spamIpWhitelist.insertOne(new Document("ip", whitelist.getAddress()).append("limit", whitelist.getLimit()));
    }

    public SpamIPWhitelist getSpamIPWhitelist(String address) {
        Document doc = spamIpWhitelist.find(Filters.eq("ip", address)).first();
        if (doc == null) return null;
        return new SpamIPWhitelist(doc.getString("ip"), doc.getInteger("limit"));
    }

    public void removeSpamIPWhitelist(String address) {
        spamIpWhitelist.deleteMany(Filters.eq("ip", address));
    }

    public int getForumMemberId(UUID uuid) {
        try {
            Document forumDoc = getPlayer(uuid, new Document("forums", 1));
            Document forums = (Document) forumDoc.get("forums");
            return forums.getInteger("member_id");
        } catch (Exception e) {
            return -1;
        }
    }

    public String getForumLinkingCode(UUID uuid) {
        try {
            Document forumDoc = getPlayer(uuid, new Document("forums", 1));
            Document forums = (Document) forumDoc.get("forums");
            return forums.getString("linking-code");
        } catch (Exception e) {
            return null;
        }
    }

    public void setForumLinkingCode(UUID uuid, int member_id, String code) {
        Document forumDoc = new Document("member_id", member_id).append("linking-code", code);
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("forums", forumDoc));
    }

    public void setForumAccountData(UUID uuid, int member_id) {
        Document forumDoc = new Document("member_id", member_id);
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("forums", forumDoc));
    }

    public void unsetForumLinkingCode(UUID uuid) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.unset("forums.linking-code"));
    }

    public void unlinkForumAccount(UUID uuid) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.unset("forums"));
    }

    public int getStaffPasswordAttempts(UUID uuid) {
    }

    public enum MongoFilter {
        UUID, USERNAME, RANK;

        public Bson getFilter(String s) {
            switch (this) {
                case UUID:
                    return Filters.eq("uuid", s);
                case USERNAME:
                    return Filters.regex("username", "^" + s + "$", "i");
                case RANK:
                    return Filters.eq("rank", s);
            }
            return null;
        }
    }
}
