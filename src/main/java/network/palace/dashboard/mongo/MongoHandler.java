package network.palace.dashboard.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import network.palace.dashboard.handlers.BseenData;
import network.palace.dashboard.handlers.Mute;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.mongo.structures.MuteStructure;
import org.bson.Document;

import java.util.*;

/**
 * @author Innectic
 * @since 9/23/2017
 */
public class MongoHandler {

    private Optional<MongoClient> client = Optional.empty();
    private Optional<MongoDatabase> database = Optional.empty();
    private Optional<MongoCollection<Document>> playerCollection = Optional.empty();
    private Optional<MongoCollection<Document>> activityCollection = Optional.empty();

    public void connect(String uri) {
        MongoClientURI connectionString = new MongoClientURI(uri);
        client = Optional.of(new MongoClient(connectionString));
        database = Optional.ofNullable(client.map(c -> c.getDatabase("palace")).orElse(null));
        playerCollection = Optional.ofNullable(database.map(d -> d.getCollection("player")).orElse(null));
        activityCollection = Optional.ofNullable(database.map(d -> d.getCollection("activity")).orElse(null));
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
        playerDocument.put("rank", player.getRank().getSqlName());
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

        playerCollection.ifPresent(c -> c.insertOne(playerDocument));
    }

    public Optional<Document> getPlayer(Player player) {
        return Optional.ofNullable(playerCollection.map(c -> c.find(
                Filters.eq("uuid", player.getUniqueId().toString())).first()).orElse(null));
    }

    public Optional<Document> getPlayer(UUID player) {
        return Optional.ofNullable(playerCollection.map(c -> c.find(
                Filters.eq("uuid", player)).first()).orElse(null));
    }

    public Optional<Document> getPlayer(UUID player, Document limit) {
        return Optional.ofNullable(playerCollection.map(c -> c.find(Filters.eq("uuid", player)).projection(limit).first()).orElse(null));
    }

    public Optional<Document> getPlayer(String player) {
        return Optional.ofNullable(playerCollection.map(c -> c.find(
                Filters.eq("username", player)).first()).orElse(null));
    }

    public Optional<Document> getPlayer(String player, Document limit) {
        return Optional.ofNullable(playerCollection.map(c -> c.find(Filters.eq("username", player)).projection(limit).first()).orElse(null));
    }

    public boolean isPlayerMuted(UUID uuid) {
        if (!playerCollection.isPresent()) return true;
        Optional<Document> playerData = getPlayer(uuid, new Document("mutes", 1));
        if (!playerData.isPresent()) return true;

        BasicDBObject mutes = (BasicDBObject) playerData.get().get("mutes");
        BasicDBObject currentMute = (BasicDBObject) mutes.get("currentMute");

        return currentMute != null;
    }

    public Optional<MuteStructure> getCurrentMute(UUID uuid) {
        if (!isPlayerMuted(uuid)) return Optional.empty();
        return Optional.ofNullable(playerCollection.map(p -> getPlayer(uuid, new Document("mutes", 1)).map(MuteStructure::from).orElse(null)).orElse(null));
    }

    public void setCurrentMute(UUID uuid, MuteStructure mute) {
        if (isPlayerMuted(uuid)) return;
        Document muteDocument = new Document(mute.get());

        Optional<Document> playerData = getPlayer(uuid, new Document("mutes", 1));
        if (!playerData.isPresent()) return;

        BasicDBObject mutes = (BasicDBObject) playerData.get().get("mutes");
        BasicDBObject currentMute = (BasicDBObject) mutes.get("currentMute");
        if (currentMute != null) return;

        mutes.put("currentMute", muteDocument);

        if (!playerCollection.isPresent()) return;
        playerCollection.get().updateOne(Filters.eq("uuid", uuid), new BasicDBObject("$set", new BasicDBObject("mutes", mutes)));
    }

    public Optional<String> nameFromUUID(UUID uuid) {
        return playerCollection.map(p -> getPlayer(uuid, new Document("username", 1)).map(d -> d.getString("username")).orElse(""));
    }

    public Optional<UUID> uuidFromName(String name) {
        return playerCollection.map(p -> getPlayer(name, new Document("uuid", 1)).map(d -> UUID.fromString(d.getString("uuid"))).orElse(null));
    }

    public List<String> getPlayersOnIP(String ip) {
        List<String> addresses = new ArrayList<>();
        if (!playerCollection.isPresent()) return addresses;
        playerCollection.get().find(Filters.eq("ipAddress", ip)).forEach((Block<Document>) document -> addresses.add(document.getString("ipAddress")));
        return addresses;
    }

    public List<UUID> getPlayersFromRank(Rank... ranks) {
        List<UUID> foundPlayers = new ArrayList<>();
        for (Rank rank : ranks) {
            if (!playerCollection.isPresent()) return foundPlayers;
            playerCollection.get().find(Filters.eq("rank", rank.getSqlName())).forEach((Block<Document>) document ->
                    foundPlayers.add(UUID.fromString(document.getString("uuid"))));
        }
        return foundPlayers;
    }

    public Rank getRank(UUID uuid) {
        return Rank.valueOf(playerCollection.map(p -> getPlayer(uuid).map(document -> document.getString("rank")))
                .orElse(Optional.of(Rank.SETTLER.getSqlName())).orElse(Rank.SETTLER.getSqlName()));
    }

    /**
     * Log activity to the collection
     *
     * @param uuid        the uuid
     * @param action      the action
     * @param description the description
     */
    public void logActivity(UUID uuid, String action, String description) {
        if (!activityCollection.isPresent()) return;
        activityCollection.get().insertOne(new Document("uuid", uuid).append("action", action).append("description", description));
    }

    public BseenData getBseenInformation(UUID uuid) {
        Optional<Document> player = getPlayer(uuid, new Document("username", 1).append("rank", 1).append("lastOnline", 1)
                .append("ip", 1).append("mutes", new Document("currentMute", 1)).append("server", 1));
        if (!player.isPresent()) return null;
        Document doc = player.get();
        Rank rank = Rank.fromString(doc.getString("rank"));
        long lastLogin = doc.getLong("lastOnline");
        String ipAddress = doc.getString("ip");
        Mute mute = new Mute(uuid, doc.getString("username"), (Document) ((BasicDBObject) doc.get("mutes"))
                .getOrDefault("currentMute", null));
        String server = doc.getString("server");
        return new BseenData(uuid, rank, lastLogin, ipAddress, mute, server);
    }

    public void disconnect() {
        client.ifPresent(MongoClient::close);
    }
}
