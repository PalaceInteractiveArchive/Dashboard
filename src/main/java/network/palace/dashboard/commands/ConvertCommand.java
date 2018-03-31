package network.palace.dashboard.commands;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.mongo.MongoHandler;
import network.palace.dashboard.utils.SqlUtil;
import org.bson.*;

import java.sql.*;
import java.util.*;

public class ConvertCommand extends DashboardCommand {

    public ConvertCommand() {
        super(Rank.MANAGER);
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            return;
        }
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            String convert = args[0];
            SqlUtil sqlUtil = dashboard.getSqlUtil();
            MongoHandler mongoHandler = dashboard.getMongoHandler();
            try (Connection connection = sqlUtil.getConnection().get()) {
                switch (convert.toLowerCase()) {
                    case "chat": {
                        player.sendMessage(ChatColor.GREEN + "Loading user data...");
                        List<UUID> players = new ArrayList<>();
                        PreparedStatement playerSql = connection.prepareStatement("SELECT user FROM chat GROUP BY user LIMIT 0,10");
                        ResultSet playerResult = playerSql.executeQuery();
                        while (playerResult.next()) {
                            players.add(UUID.fromString(playerResult.getString("user")));
                        }
                        playerResult.close();
                        playerSql.close();
                        player.sendMessage(ChatColor.GREEN + "User data loaded! " + players.size() + " players.");
                        player.sendMessage(ChatColor.GREEN + "Processing players...");
                        for (UUID uuid : players) {
                            BsonArray messages = new BsonArray();
                            PreparedStatement sql = connection.prepareStatement("SELECT message,timestamp FROM chat WHERE user=? ORDER BY id ASC");
                            sql.setString(1, uuid.toString());
                            ResultSet result = sql.executeQuery();
                            while (result.next()) {
                                long time = result.getTimestamp("timestamp").getTime();
                                messages.add(new BsonDocument("message", new BsonString(result.getString("message")))
                                        .append("time", new BsonInt64(time / 1000)));
                            }
                            result.close();
                            sql.close();
                            if (messages.isEmpty()) continue;
                            dashboard.getMongoHandler().getChatCollection().updateOne(MongoHandler.MongoFilter.UUID.getFilter(uuid.toString()),
                                    Updates.pushEach("messages", messages), new UpdateOptions().upsert(true));
                            player.sendMessage(ChatColor.GREEN + "Finished " + uuid.toString() + " (" + messages.size() + " messages)");
                        }
                        player.sendMessage(ChatColor.GREEN + "Finished processing players!");
                        break;
                    }
                    case "friends": {
                        player.sendMessage(ChatColor.GREEN + "Loading friend data...");
                        List<Document> friends = new ArrayList<>();
                        PreparedStatement sql = connection.prepareStatement("SELECT * FROM friends;");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            try {
                                Document doc = new Document("sender", UUID.fromString(result.getString("sender")).toString())
                                        .append("receiver", UUID.fromString(result.getString("receiver")).toString());
                                if (result.getInt("status") == 1) {
                                    doc.append("started", System.currentTimeMillis());
                                } else {
                                    doc.append("started", 0L);
                                }
                                friends.add(doc);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Friend data loaded!");
                        player.sendMessage(ChatColor.GREEN + "Inserting into database...");
                        long t = System.currentTimeMillis();
                        mongoHandler.getFriendsCollection().insertMany(friends);
                        player.sendMessage(ChatColor.GREEN + "Inserted into database in " + (System.currentTimeMillis() - t) + "ms");
                        break;
                    }
                    case "warps": {
                        player.sendMessage(ChatColor.GREEN + "Loading warp data...");
                        List<Document> warps = new ArrayList<>();
                        PreparedStatement sql = connection.prepareStatement("SELECT * FROM warps;");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            warps.add(new Document("name", result.getString("name").toLowerCase())
                                    .append("server", result.getString("server"))
                                    .append("x", result.getDouble("x"))
                                    .append("y", result.getDouble("y"))
                                    .append("z", result.getDouble("z"))
                                    .append("yaw", result.getInt("yaw"))
                                    .append("pitch", result.getInt("pitch"))
                                    .append("world", result.getString("world")));
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Warp data loaded!");
                        player.sendMessage(ChatColor.GREEN + "Inserting into database...");
                        long t = System.currentTimeMillis();
                        mongoHandler.getWarpsCollection().insertMany(warps);
                        player.sendMessage(ChatColor.GREEN + "Inserted into database in " + (System.currentTimeMillis() - t) + "ms");
                        break;
                    }
                    case "players": {
                        player.sendMessage(ChatColor.GREEN + "Loading ignore data...");
                        List<IgnoreData> ignoreList = new ArrayList<>();
                        PreparedStatement ignoreSql = connection.prepareStatement("SELECT * FROM ignored_players;");
                        ResultSet ignoreResult = ignoreSql.executeQuery();
                        while (ignoreResult.next()) {
                            IgnoreData data = new IgnoreData(UUID.fromString(ignoreResult.getString("uuid")),
                                    UUID.fromString(ignoreResult.getString("ignored")),
                                    ignoreResult.getLong("started") * 1000);
                            ignoreList.add(data);
                        }
                        ignoreResult.close();
                        ignoreSql.close();
                        player.sendMessage(ChatColor.GREEN + "Ignore data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading ban data...");
                        List<Ban> banList = new ArrayList<>();
                        PreparedStatement banSql = connection.prepareStatement("SELECT * FROM banned_players;");
                        ResultSet banResult = banSql.executeQuery();
                        while (banResult.next()) {
                            UUID uuid = UUID.fromString(banResult.getString("uuid"));
                            String reason = banResult.getString("reason").trim();
                            boolean permanent = banResult.getInt("permanent") == 1;
                            Timestamp releaseTimestamp = banResult.getTimestamp("release");
                            long release = releaseTimestamp.getTime();
                            String source = banResult.getString("source");
                            boolean active = banResult.getInt("active") == 1;
                            Ban ban = new Ban(uuid, "", permanent, release, release, reason, source);
                            ban.setActive(active);
                            banList.add(ban);
                        }
                        banResult.close();
                        banSql.close();
                        player.sendMessage(ChatColor.GREEN + "Ban data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading mute data...");
                        List<Mute> muteList = new ArrayList<>();
                        PreparedStatement muteSql = connection.prepareStatement("SELECT * FROM muted_players;");
                        ResultSet muteResult = muteSql.executeQuery();
                        while (muteResult.next()) {
                            UUID uuid = UUID.fromString(muteResult.getString("uuid"));
                            String reason = muteResult.getString("reason").trim();
                            Timestamp releaseTimestamp = muteResult.getTimestamp("release");
                            long release = releaseTimestamp.getTime();
                            String source = muteResult.getString("source");
                            boolean active = muteResult.getInt("active") == 1;
                            Mute mute = new Mute(uuid, "", active, release, release, reason, source);
                            muteList.add(mute);
                        }
                        muteResult.close();
                        muteSql.close();
                        player.sendMessage(ChatColor.GREEN + "Mute data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading kick data...");
                        List<Kick> kickList = new ArrayList<>();
                        PreparedStatement kickSql = connection.prepareStatement("SELECT * FROM kicks;");
                        ResultSet kickResult = kickSql.executeQuery();
                        while (kickResult.next()) {
                            UUID uuid = UUID.fromString(kickResult.getString("uuid"));
                            String reason = kickResult.getString("reason").trim();
                            Timestamp timestamp = kickResult.getTimestamp("time");
                            long time = timestamp.getTime();
                            String source = kickResult.getString("source");
                            Kick kick = new Kick(uuid, reason, source, time);
                            kickList.add(kick);
                        }
                        kickResult.close();
                        kickSql.close();
                        player.sendMessage(ChatColor.GREEN + "Kick data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading cosmetics data...");
                        HashMap<UUID, List<Integer>> cosmeticsMap = new HashMap<>();
                        PreparedStatement cosmeticSql = connection.prepareStatement("SELECT * FROM cosmetics;");
                        ResultSet cosmeticResult = cosmeticSql.executeQuery();
                        while (cosmeticResult.next()) {
                            UUID uuid = UUID.fromString(cosmeticResult.getString("uuid"));
                            int cosmetic = cosmeticResult.getInt("cosmetic");
                            List<Integer> list;
                            if (cosmeticsMap.containsKey(uuid)) {
                                list = new ArrayList<>(cosmeticsMap.get(uuid));
                            } else {
                                list = new ArrayList<>();
                            }
                            list.add(cosmetic);
                            cosmeticsMap.put(uuid, list);
                        }
                        cosmeticResult.close();
                        cosmeticSql.close();
                        player.sendMessage(ChatColor.GREEN + "Cosmetics data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading outfit purchases data...");
                        HashMap<UUID, List<Purchase>> outfitMap = new HashMap<>();
                        PreparedStatement outfitSql = connection.prepareStatement("SELECT * FROM purchases;");
                        ResultSet outfitResult = outfitSql.executeQuery();
                        while (outfitResult.next()) {
                            UUID uuid = UUID.fromString(outfitResult.getString("uuid"));
                            int item = outfitResult.getInt("item");
                            long time = outfitResult.getLong("time");
                            List<Purchase> list;
                            if (outfitMap.containsKey(uuid)) {
                                list = new ArrayList<>(outfitMap.get(uuid));
                            } else {
                                list = new ArrayList<>();
                            }
                            list.add(new Purchase(item, time));
                            outfitMap.put(uuid, list);
                        }
                        cosmeticResult.close();
                        cosmeticSql.close();
                        player.sendMessage(ChatColor.GREEN + "Outfit purchases data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Processing players...!");
                        int start = 0;
                        PreparedStatement playerSql = connection.prepareStatement("SELECT * FROM player_data WHERE ipAddress!='no ip' AND username='Legobuilder0813' GROUP BY uuid ORDER BY id ASC LIMIT " + start + ",1;");
                        ResultSet playerResult = playerSql.executeQuery();
                        List<Document> documents = new ArrayList<>();
                        while (playerResult.next()) {
                            UUID uuid = UUID.fromString(playerResult.getString("uuid"));
                            String username = playerResult.getString("username");
                            Rank rank = Rank.fromString(playerResult.getString("rank"));
                            player.sendMessage(ChatColor.GREEN + "Starting processing " + username + "...");
                            Document playerDocument = new Document();
                            playerDocument.put("uuid", uuid.toString());
                            playerDocument.put("username", username);
                            playerDocument.put("previousNames", new ArrayList<>());
                            playerDocument.put("balance", playerResult.getInt("balance"));
                            playerDocument.put("tokens", playerResult.getInt("tokens"));
                            playerDocument.put("server", playerResult.getString("server"));
                            playerDocument.put("isp", playerResult.getString("isp"));
                            playerDocument.put("country", playerResult.getString("country"));
                            playerDocument.put("region", playerResult.getString("region"));
                            playerDocument.put("regionName", playerResult.getString("regionName"));
                            playerDocument.put("timezone", playerResult.getString("timezone"));
                            playerDocument.put("lang", "en_us");
                            playerDocument.put("minecraftVersion", playerResult.getInt("mcversion"));
                            playerDocument.put("honor", playerResult.getInt("honor"));
                            playerDocument.put("ip", playerResult.getString("ipAddress"));
                            playerDocument.put("rank", rank.getDBName());
                            Timestamp lastSeen = playerResult.getTimestamp("lastseen");
                            playerDocument.put("lastOnline", lastSeen.getTime());
                            playerDocument.put("onlineTime", playerResult.getInt("onlinetime"));

                            Map<String, String> skinData = new HashMap<>();
                            skinData.put("hash", "");
                            skinData.put("signature", "");
                            playerDocument.put("skin", skinData);

                            List<Integer> cosmeticData = new ArrayList<>();
                            if (cosmeticsMap.containsKey(uuid)) {
                                cosmeticData = cosmeticsMap.remove(uuid);
                            }
                            playerDocument.put("cosmetics", cosmeticData);

                            List<Object> kicks = new ArrayList<>();
                            List<Object> mutes = new ArrayList<>();
                            List<Object> bans = new ArrayList<>();

                            for (Kick kick : kickList) {
                                if (!kick.getUniqueId().equals(uuid)) continue;
                                Document kickDoc = new Document("reason", kick.getReason()).append("time", kick.getTime())
                                        .append("source", kick.getSource());
                                kicks.add(kickDoc);
                            }

                            for (Mute mute : muteList) {
                                if (!mute.getUniqueId().equals(uuid)) continue;
                                Document muteDoc = new Document("created", mute.getCreated()).append("expires", mute.getExpires())
                                        .append("reason", mute.getReason()).append("source", mute.getSource())
                                        .append("active", mute.isMuted());
                                mutes.add(muteDoc);
                            }

                            for (Ban ban : banList) {
                                if (!ban.getUniqueId().equals(uuid)) continue;
                                Document muteDoc = new Document("created", ban.getCreated()).append("expires", ban.getExpires())
                                        .append("permanent", ban.isPermanent()).append("reason", ban.getReason())
                                        .append("source", ban.getSource()).append("active", ban.isActive());
                                bans.add(muteDoc);
                            }

                            playerDocument.put("kicks", kicks);
                            playerDocument.put("mutes", mutes);
                            playerDocument.put("bans", bans);

                            Map<String, Object> parkData = new HashMap<>();
                            List<Object> inventoryData = new ArrayList<>();
                            parkData.put("inventories", inventoryData);

                            Map<String, String> magicBandData = new HashMap<>();
                            magicBandData.put("bandtype", playerResult.getString("bandcolor"));
                            magicBandData.put("namecolor", playerResult.getString("namecolor"));
                            parkData.put("magicband", magicBandData);

                            Map<String, Integer> fpData = new HashMap<>();
                            fpData.put("slow", playerResult.getInt("slow"));
                            fpData.put("moderate", playerResult.getInt("moderate"));
                            fpData.put("thrill", playerResult.getInt("thrill"));
                            fpData.put("sday", playerResult.getInt("sday"));
                            fpData.put("mday", playerResult.getInt("mday"));
                            fpData.put("tday", playerResult.getInt("tday"));
                            parkData.put("fastpass", fpData);

                            List<Object> rideData = new ArrayList<>();
                            parkData.put("rides", rideData);

                            parkData.put("outfit", playerResult.getString("outfit"));
                            List<Object> outfitData = new ArrayList<>();
                            if (outfitMap.containsKey(uuid)) {
                                for (Purchase p : outfitMap.remove(uuid)) {
                                    outfitData.add(new Document("id", p.getItem()).append("time", p.getTime() * 1000));
                                }
                            }
                            parkData.put("outfitPurchases", outfitData);

                            Map<String, Object> parkSettings = new HashMap<>();
                            parkSettings.put("visibility", playerResult.getInt("visibility") == 1);
                            parkSettings.put("flash", playerResult.getInt("flash") == 1);
                            parkSettings.put("hotel", playerResult.getInt("hotel") == 1);
                            parkSettings.put("pack", playerResult.getString("pack"));
                            parkData.put("settings", parkSettings);

                            playerDocument.put("parks", parkData);

                            Map<String, Object> voteData = new HashMap<>();
                            voteData.put("lastTime", playerResult.getLong("vote"));
                            voteData.put("lastSite", playerResult.getInt("lastvote"));
                            playerDocument.put("vote", voteData);

                            Map<String, Long> monthlyRewards = new HashMap<>();
                            switch (rank) {
                                case MANAGER:
                                case ADMIN:
                                case DEVELOPER:
                                case SRMOD:
                                case MOD:
                                case TRAINEE:
                                case CHARACTER:
                                case SPECIALGUEST:
                                case HONORABLE:
                                    monthlyRewards.put("honorable", playerResult.getLong("monthhonorable"));
                                case MAJESTIC:
                                    monthlyRewards.put("majestic", playerResult.getLong("monthmajestic"));
                                case NOBLE:
                                    monthlyRewards.put("noble", playerResult.getLong("monthnoble"));
                                case SHAREHOLDER:
                                case DWELLER:
                                case DVCMEMBER:
                                    monthlyRewards.put("dweller", playerResult.getLong("monthdweller"));
                                case SETTLER:
                                    monthlyRewards.put("settler", playerResult.getLong("monthsettler"));
                            }
                            playerDocument.put("monthlyRewards", monthlyRewards);

                            playerDocument.put("tutorial", playerResult.getInt("tutorial") == 1);

                            Map<String, Object> settings = new HashMap<>();
                            settings.put("mentions", playerResult.getInt("mentions") == 1);
                            settings.put("friendRequestToggle", playerResult.getInt("toggled") == 1);
                            playerDocument.put("settings", settings);

                            List<Object> achievements = new ArrayList<>();
                            playerDocument.put("achievements", achievements);

                            List<Object> autographs = new ArrayList<>();
                            playerDocument.put("autographs", autographs);

                            List<Object> transactions = new ArrayList<>();
                            playerDocument.put("transactions", transactions);

                            List<Object> ignoring = new ArrayList<>();
                            for (IgnoreData data : ignoreList) {
                                if (!data.getUuid().equals(uuid)) continue;
                                Document ignoreDoc = new Document("uuid", data.getIgnored().toString())
                                        .append("started", data.getStarted());
                            }
                            playerDocument.put("ignoring", ignoring);

                            documents.add(playerDocument);
                            player.sendMessage(ChatColor.GREEN + "Finished " + username);
                            player.sendMessage(ChatColor.GREEN + "Requesting past usernames...");
                            mongoHandler.updatePreviousUsernames(uuid, username);
                            player.sendMessage(ChatColor.GREEN + "Past usernames updated!");
                        }
                        playerResult.close();
                        playerSql.close();
                        player.sendMessage(ChatColor.GREEN + "Inserting into database...");
                        long t = System.currentTimeMillis();
                        mongoHandler.getPlayerCollection().insertMany(documents);
                        player.sendMessage(ChatColor.GREEN + "Inserted into database in " + (System.currentTimeMillis() - t) + "ms");
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class Purchase {
        private int item;
        private long time;
    }
}
