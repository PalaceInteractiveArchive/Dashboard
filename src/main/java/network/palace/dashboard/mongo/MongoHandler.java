package network.palace.dashboard.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
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
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketPlayerRank;
import network.palace.dashboard.packets.inventory.Resort;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;
import network.palace.dashboard.utils.IPUtil;
import network.palace.dashboard.utils.InventoryUtil;
import network.palace.dashboard.utils.MCLeakUtil;
import org.bson.*;
import org.bson.conversions.Bson;

import java.util.*;

/**
 * @author Innectic
 * @since 9/23/2017
 */
public class MongoHandler {
    private MongoClient client = null;
    @Getter private MongoDatabase database = null;
    @Getter private MongoCollection<Document> playerCollection = null;
    @Getter private MongoCollection<Document> chatCollection = null;
    @Getter private MongoCollection<Document> activityCollection = null;
    @Getter private MongoCollection<Document> friendsCollection = null;
    @Getter private MongoCollection<Document> bansCollection = null;
    @Getter private MongoCollection<Document> permissionCollection = null;
    @Getter private MongoCollection<Document> cosmeticsCollection = null;
    @Getter private MongoCollection<Document> resourcePackCollection = null;
    @Getter private MongoCollection<Document> honorMappingCollection = null;
    @Getter private MongoCollection<Document> outfitsCollection = null;
    @Getter private MongoCollection<Document> hotelCollection = null;
    @Getter private MongoCollection<Document> serversCollection = null;
    @Getter private MongoCollection<Document> votingCollection = null;
    @Getter private MongoCollection<Document> warpsCollection = null;

    public void connect(String uri) {
        MongoClientURI connectionString = new MongoClientURI(uri);
        client = new MongoClient(connectionString);
        database = client.getDatabase("palace");
        playerCollection = database.getCollection("player");
        chatCollection = database.getCollection("chat");
        activityCollection = database.getCollection("activity");
        friendsCollection = database.getCollection("friends");
        bansCollection = database.getCollection("bans");
        permissionCollection = database.getCollection("permissions");
        cosmeticsCollection = database.getCollection("cosmetics");
        resourcePackCollection = database.getCollection("resourcepacks");
        honorMappingCollection = database.getCollection("honormapping");
        outfitsCollection = database.getCollection("outfits");
        hotelCollection = database.getCollection("hotels");
        serversCollection = database.getCollection("servers");
        votingCollection = database.getCollection("voting");
        warpsCollection = database.getCollection("warps");
    }

    public void createPlayer(Player player) {
        Document playerDocument = new Document();
        playerDocument.put("uuid", player.getUniqueId());
        playerDocument.put("username", player.getUsername());
        playerDocument.put("tokens", 0);
        playerDocument.put("balance", 250);
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
        playerDocument.put("isVisible", true); // XXX: Maybe this should be in redis
        playerDocument.put("staffPassword", null);
        playerDocument.put("tutorial", true);
        playerDocument.put("kicks", new ArrayList<>());

        Map<String, String> skinData = new HashMap<>();
        skinData.put("hash", "");
        skinData.put("signature", "");
        playerDocument.put("skin", skinData);

        Map<String, Object> mutes = new HashMap<>();
        mutes.put("currentMute", null);
        mutes.put("previousMutes", new ArrayList<>());
        playerDocument.put("mutes", mutes);

        Map<String, Object> bans = new HashMap<>();
        bans.put("currentBan", null);
        bans.put("previousBans", new ArrayList<>());
        playerDocument.put("bans", bans);

        Map<String, Object> parks = new HashMap<>();
        parks.put("inventories", new ArrayList<>()); // TODO: @lego
        parks.put("buildMode", false);
        Map<String, Object> fastPass = new HashMap<>();
        fastPass.put("slow", 0);
        fastPass.put("moderate", 0);
        fastPass.put("thrill", 0);
        parks.put("fastpass", fastPass);
        parks.put("outfit", "0,0,0,0");
        parks.put("outfitPurchases", new ArrayList<>());

        Map<String, Object> settings = new HashMap<>();
        settings.put("visibility", true);
        settings.put("flash", true);
        settings.put("hotel", true);
        parks.put("settings", settings);
        playerDocument.put("parks", parks);

        playerDocument.put("gameData", new HashMap<>());

        Map<String, Integer> monthlyRewards = new HashMap<>();
        monthlyRewards.put("settler", 0);
        monthlyRewards.put("dweller", 0);
        monthlyRewards.put("noble", 0);
        monthlyRewards.put("majestic", 0);
        monthlyRewards.put("honorable", 0);
        playerDocument.put("monthlyRewards", monthlyRewards);

        playerDocument.put("achievements", new ArrayList<>());
        playerDocument.put("autographs", new ArrayList<>());
        playerDocument.put("rides", new ArrayList<>());
        playerDocument.put("transactions", new ArrayList<>());

        playerCollection.insertOne(playerDocument);
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

    public boolean isPlayerMuted(UUID uuid) {
        return getCurrentMute(uuid).isMuted();
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
        BsonArray array = new BsonArray();
        for (Object o : getPlayer(uuid, new Document("mutes", 1)).get("mutes", ArrayList.class)) {
            BsonDocument doc = (BsonDocument) o;
            doc.put("active", BsonBoolean.valueOf(false));
            array.add(doc);
        }
        playerCollection.updateOne(Filters.eq("uuid", uuid), new Document("mutes", array));
    }

    public void mutePlayer(UUID uuid, Mute mute) {
        if (isPlayerMuted(uuid)) return;

        Document muteDocument = new Document("created", mute.getCreated()).append("expires", mute.getExpires())
                .append("reason", mute.getReason()).append("source", mute.getSource()).append("active", true);

        playerCollection.updateOne(Filters.eq("uuid", uuid), Updates.push("mutes", muteDocument));
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
        return new Ban(uuid, name, null);
    }

    public void unbanPlayer(UUID uuid) {
        BsonArray array = new BsonArray();
        for (Object o : getPlayer(uuid, new Document("bans", 1)).get("bans", ArrayList.class)) {
            BsonDocument doc = (BsonDocument) o;
            doc.put("active", BsonBoolean.valueOf(false));
            array.add(doc);
        }
        playerCollection.updateOne(Filters.eq("uuid", uuid), new Document("bans", array));
    }

    public void banPlayer(UUID uuid, Ban ban) {
        if (isPlayerBanned(uuid)) return;

        Document banDocument = new Document("created", ban.getCreated()).append("expires", ban.getExpires())
                .append("permanent", ban.isPermanent()).append("reason", ban.getReason())
                .append("source", ban.getSource()).append("active", true);

        playerCollection.updateOne(Filters.eq("uuid", uuid), Updates.push("bans", banDocument));
    }

    public void banProvider(ProviderBan ban) {
        bansCollection.insertOne(new Document("type", "provider").append("data", ban.getProvider())
                .append("source", ban.getSource()));
    }

    public ProviderBan getProviderBan(String isp) {
        Document doc = bansCollection.find(new Document("type", "provider").append("data", isp)).first();
        return new ProviderBan(doc.getString("data"), doc.getString("source"));
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
        return new AddressBan(doc.getString("data"), doc.getString("reason"), doc.getString("source"));
    }

    public void unbanAddress(String address) {
        bansCollection.deleteMany(new Document("type", "ip").append("data", address));
    }

    public void updateProviderData(UUID uuid, IPUtil.ProviderData data) {
        Document doc = new Document("isp", data.getIsp()).append("country", data.getCountry())
                .append("region", data.getRegion()).append("regionName", data.getRegionName())
                .append("timezone", data.getTimezone());
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), doc);
    }

    public void login(Player player) {
        login(player, false);
    }

    public void login(Player player, boolean silent) {
        Dashboard dashboard = Launcher.getDashboard();

        if (!silent) {
            dashboard.getSchedulerManager().runAsync(() -> {
                // Check if the uuid is from MCLeaks before we continue.
                boolean isMCLeaks = MCLeakUtil.checkPlayer(player);
                if (isMCLeaks) {
                    // UUID is in MCLeaks, temp ban the account
                    Ban ban = new Ban(player.getUniqueId(), player.getUsername(), true, System.currentTimeMillis(), "MCLeaks Account", "Dashboard");
                    banPlayer(player.getUniqueId(), ban);
                    dashboard.getModerationUtil().announceBan(ban);
                    player.kickPlayer(ChatColor.RED + "MCLeaks Accounts are not allowed on the Palace Network\n" +
                            ChatColor.AQUA + "If you think were banned incorrectly, submit an appeal at palnet.us/appeal");
                }
            });
        }

        dashboard.getSchedulerManager().runAsync(() -> {
//            PreparedStatement sql = connection.prepareStatement("SELECT rank,ipAddress,username,friendRequestToggle,mentions,onlinetime,tutorial,mcversion FROM player_data WHERE uuid=?");
            Document doc = getPlayer(player.getUniqueId(), new Document("rank", 1).append("ip", 1)
                    .append("username", 1).append("friendRequestToggle", 1).append("mentions", 1).append("onlinetime", 1)
                    .append("tutorial", 1).append("mcversion", 1).append("settings", 1));
            long ot = doc.getLong("onlinetime");
            player.setOnlineTime(ot == 0 ? 1 : ot);
            Rank rank = Rank.fromString(doc.getString("rank"));
            if (!rank.equals(Rank.SETTLER)) {
                PacketPlayerRank packet = new PacketPlayerRank(player.getUniqueId(), rank);
                player.send(packet);
            }
            boolean needsUpdate = false;
            boolean isDifferentIP = !player.getAddress().equals(doc.getString("ip"));
            boolean disable = isDifferentIP && rank.getRankId() >= Rank.TRAINEE.getRankId();

            int protocolVersion = doc.getInteger("mcversion");

            if (isDifferentIP || !player.getUsername().equals(doc.getString("username")) ||
                    player.getMcVersion() != protocolVersion) needsUpdate = true;

            Document settings = (Document) doc.get("settings");

            player.setDisabled(disable);
            player.setRank(rank);
            player.setFriendRequestToggle(settings.getBoolean("friendRequestToggle"));
            player.setMentions(settings.getBoolean("mentions"));
            player.setNewGuest(doc.getBoolean("tutorial"));
            dashboard.addPlayer(player);
            dashboard.getPlayerLog().info("New Player Object for UUID " + player.getUniqueId() + " username " + player.getUsername() + " Source: MongoHandler.login");
            dashboard.addToCache(player.getUniqueId(), player.getUsername());

            String username = doc.getString("username");
            if (!silent) {
                if (needsUpdate) {
                    update(player, doc.getString("ip"), username, protocolVersion);
                }
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                            rank.getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked in.";
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
            }

            List<IgnoreData> ignored = getIgnoreData(player);
            player.setIgnoredUsers(ignored);

            HashMap<UUID, String> friends = getFriendList(player.getUniqueId());
            HashMap<UUID, String> requests = getRequestList(player.getUniqueId());
            player.setFriends(friends);
            player.setRequests(requests);
            HashMap<UUID, String> friendList = player.getFriends();
            if (friendList != null && !silent) {
                String joinMessage = rank.getTagColor() + player.getUsername() + ChatColor.LIGHT_PURPLE + " has joined.";
                dashboard.getFriendUtil().friendMessage(player, friendList, joinMessage);
            }
            Mute mute = getCurrentMute(player.getUniqueId(), player.getUsername());
            player.setMute(mute);
            if (disable) {
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment(rank.getName() + " " + player.getUsername() +
                        " connected from a new IP address " + player.getAddress());
                a.color("warning");
                dashboard.getSlackUtil().sendDashboardMessage(m, Arrays.asList(a), false);
                player.sendMessage(ChatColor.YELLOW + "\n\n" + ChatColor.BOLD +
                        "You connected with a new IP address, type " + ChatColor.GREEN + "" + ChatColor.BOLD +
                        "/staff login [password]" + ChatColor.YELLOW + "" + ChatColor.BOLD + " to verify your account.\n");
            }
        });
    }

    private void update(Player player, String ipAddress, String username, int mcversion) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(player.getUniqueId().toString()),
                new Document("ip", ipAddress).append("username", username).append("mcversion", mcversion));
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
            if (list.first() == null) return null;
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
            if (list.first() == null) return null;
            return list.first().getString("username");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public List<String> getPlayersOnIP(String ip) {
        List<String> addresses = new ArrayList<>();

        playerCollection.find(Filters.eq("ipAddress", ip))
                .forEach((Block<Document>) document -> addresses.add(document.getString("ipAddress")));
        return addresses;
    }

    public List<UUID> getPlayersByRank(Rank... ranks) {
        List<UUID> foundPlayers = new ArrayList<>();
        for (Rank rank : ranks) {
            if (playerCollection != null) return foundPlayers;
            playerCollection.find(Filters.eq("rank", rank.getDBName())).forEach((Block<Document>) document ->
                    foundPlayers.add(UUID.fromString(document.getString("uuid"))));
        }
        return foundPlayers;
    }

    public List<String> getPlayerNamesFromRank(Rank rank) {
        List<String> list = new ArrayList<>();
        playerCollection.find(MongoFilter.RANK.getFilter(rank.getDBName())).projection(new Document("username", 1))
                .forEach((Block<Document>) d -> list.add(d.getString("username")));
        return list;
    }

    public List<UUID> getPlayerUUIDsFromRank(Rank rank) {
        List<UUID> list = new ArrayList<>();
        playerCollection.find(MongoFilter.RANK.getFilter(rank.getDBName())).projection(new Document("uuid", 1))
                .forEach((Block<Document>) d -> list.add(UUID.fromString(d.getString("uuid"))));
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
        Document result = playerCollection.find(MongoFilter.UUID.getFilter(uuid.toString())).first();
        if (result == null) return Rank.SETTLER;
        return Rank.fromString(result.getString("rank"));
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
        Document player = getPlayer(uuid, new Document("username", 1).append("rank", 1).append("lastOnline", 1)
                .append("ip", 1).append("mutes", new Document("currentMute", 1)).append("server", 1));
        if (player == null) return null;
        Rank rank = Rank.fromString(player.getString("rank"));
        long lastLogin = player.getLong("lastOnline");
        String ipAddress = player.getString("ip");
        Mute mute = new Mute(uuid, player.getString("username"), (Document) ((BasicDBObject) player.get("mutes"))
                .getOrDefault("currentMute", null));
        String server = player.getString("server");
        return new BseenData(uuid, rank, lastLogin, ipAddress, mute, server);
    }

    public void staffClock(UUID uuid, boolean b) {
    }

    public void ignorePlayer(Player player, UUID uuid) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(player.getUniqueId().toString()), Updates.push("ignoring", new Document("uuid", uuid.toString()).append("started", System.currentTimeMillis())));
    }

    public void unignorePlayer(Player player, UUID uuid) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(player.getUniqueId().toString()), Updates.pull("ignoring", new Document("uuid", uuid.toString())));
    }

    public void completeTutorial(UUID uuid) {
        playerCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.set("tutorial", true));
    }

    public List<Server> getServers() {
        List<Server> list = new ArrayList<>();
        for (Document doc : serversCollection.find()) {
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
            if ((id == 0 && !friend) || (id == 1 && friend)) {
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
                .append("started", 0));
    }

    public void removeFriend(UUID sender, UUID receiver) {
        friendsCollection.deleteOne(Filters.or(
                new Document("sender", sender.toString()).append("receiver", receiver.toString()),
                new Document("receiver", sender.toString()).append("sender", receiver.toString())
        ));
    }

    public void acceptFriendRequest(UUID receiver, UUID sender) {
        friendsCollection.updateOne(new Document("sender", sender.toString()).append("receiver", receiver.toString()),
                new Document("started", System.currentTimeMillis()));
    }

    public void denyFriendRequest(UUID receiver, UUID sender) {
        friendsCollection.deleteOne(new Document("sender", sender.toString()).append("receiver", receiver.toString()).append("started", 0));
    }

    public boolean getFriendRequestToggle(UUID uuid) {
        return getPlayer(uuid, new Document("settings.friendRequestToggle", 1)).getBoolean("settings.friendRequestToggle");
    }

    public void disconnect() {
        client.close();
    }

    public void logChat(UUID uuid, List<String> list) {
        if (list.isEmpty()) return;
        BsonArray array = new BsonArray();
        for (int i = 1; i < list.size(); i++) {
            array.add(new BsonDocument("message", new BsonString(list.remove(i))).append("time", new BsonInt64(System.currentTimeMillis() / 1000)));
        }
        chatCollection.updateOne(MongoFilter.UUID.getFilter(uuid.toString()), Updates.push("messages", array),
                new UpdateOptions().upsert(true));
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
     * @param limit a document specifying the limits of the search
     * @return a document with the requested data
     */
    public Document getParkData(UUID uuid, Document limit) {
        return getPlayer(uuid, new Document("parks", limit == null ? 1 : limit));
    }

    public Document getParkInventoryData(UUID uuid) {
        return getParkData(uuid, new Document("inventories", 1));
    }

    public Document getParkInventory(UUID uuid, Resort resort) {
        return playerCollection.find(new Document("uuid", uuid.toString()).append("parks.inventories.$.resort", resort.getId())).first();
    }

    public void setInventoryData(UUID uuid, ResortInventory inv) {
        UpdateData data = InventoryUtil.getDataFromJson(inv.getBackpackJSON(), inv.getBackpackSize(),
                inv.getLockerJSON(), inv.getLockerSize(), inv.getHotbarJSON());
        setInventoryData(uuid, inv.getResort(), data);
    }

    public void setInventoryData(UUID uuid, Resort resort, UpdateData data) {
        Document doc = new Document("packcontents", data.getPack()).append("packsize", data.getPackSize())
                .append("lockercontents", data.getLocker()).append("lockersize", data.getLockerSize())
                .append("hotbarcontents", data.getHotbar()).append("resort", resort);
        playerCollection.updateOne(new Document("uuid", uuid.toString()).append("parks.inventories.$.resort", resort),
                new Document("parks.inventories.$", doc));
    }

    public void updateInventoryData(UUID uuid, InventoryUpdate update) {
        HashMap<Resort, UpdateData> map = update.getMap();
        for (Map.Entry<Resort, UpdateData> entry : map.entrySet()) {
            Resort resort = entry.getKey();
            UpdateData data = entry.getValue();
            setInventoryData(uuid, resort, data);
        }
    }

    public enum MongoFilter {
        UUID, USERNAME, RANK;

        public Bson getFilter(String s) {
            switch (this) {
                case UUID:
                    return Filters.eq("uuid", s);
                case USERNAME:
                    return Filters.regex("username", s, "i");
                case RANK:
                    return Filters.eq("rank", s);
            }
            return null;
        }
    }
}
