package network.palace.dashboard.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.mongo.MongoHandler;
import network.palace.dashboard.packets.inventory.Resort;
import network.palace.dashboard.utils.InventoryUtil;
import network.palace.dashboard.utils.SqlUtil;
import org.bson.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

public class ConvertCommand extends DashboardCommand {

    public ConvertCommand() {
        super(Rank.MANAGER);
    }

    public static long roundUp(long num, long divisor) {
        return (num + divisor - 1) / divisor;
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.GREEN + "/convert [bans, activity, chat, friends, warps, players]");
            return;
        }
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            String convert = args[0];
            SqlUtil sqlUtil = dashboard.getSqlUtil();
            MongoHandler mongoHandler = dashboard.getMongoHandler();
            try (Connection connection = sqlUtil.getConnection().get()) {
                switch (convert.toLowerCase()) {
                    case "bans": {
                        player.sendMessage(ChatColor.GREEN + "Loading ban data...");
                        List<Document> banData = new ArrayList<>();
                        PreparedStatement ipSql = connection.prepareStatement("SELECT * FROM banned_ips WHERE active=1;");
                        ResultSet ipResult = ipSql.executeQuery();
                        while (ipResult.next()) {
                            Document doc = new Document("type", "ip").append("data", ipResult.getString("ipAddress"))
                                    .append("reason", ipResult.getString("reason").trim())
                                    .append("source", ipResult.getString("source").trim());
                            banData.add(doc);
                        }
                        ipResult.close();
                        ipSql.close();
                        player.sendMessage(ChatColor.GREEN + "Ban data loaded! " + banData.size() + " entries");
                        player.sendMessage(ChatColor.GREEN + "Updating Mongo ban data...");
                        mongoHandler.getBansCollection().insertMany(banData);
                        player.sendMessage(ChatColor.GREEN + "Finished updating Mongo ban data!");
                        break;
                    }
                    case "activity": {
                        player.sendMessage(ChatColor.GREEN + "Loading activity data...");
                        List<Document> activityData = new ArrayList<>();
                        int min = Integer.parseInt(args[1]);
                        int max = Integer.parseInt(args[2]);
                        PreparedStatement sql = connection.prepareStatement("SELECT * FROM activity LIMIT " + min + "," + max + ";");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            UUID uuid;
                            try {
                                uuid = UUID.fromString(result.getString("uuid"));
                            } catch (Exception e) {
                                continue;
                            }
                            String action = result.getString("action");
                            String description = result.getString("description");
                            long time = result.getTimestamp("time").getTime();
                            Document entry = new Document("uuid", uuid.toString()).append("action", action)
                                    .append("description", description).append("time", time);
                            activityData.add(entry);
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Activity data loaded! " + activityData.size() + " entries");
                        player.sendMessage(ChatColor.GREEN + "Updating Mongo activity data...");
                        mongoHandler.getActivityCollection().insertMany(activityData);
                        player.sendMessage(ChatColor.GREEN + "Finished updating Mongo activity data!");
                        break;
                    }
                    /*case "transactions": {
                        player.sendMessage(ChatColor.GREEN + "Loading transaction data...");
                        HashMap<UUID, BsonArray> transactionData = new HashMap<>();
                        PreparedStatement sql = connection.prepareStatement("SELECT * FROM economy_logs;");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            UUID uuid = UUID.fromString(result.getString("uuid"));
                            int amount = result.getInt("amount");
                            String type = result.getString("type");
                            String source = result.getString("source");
                            String server = result.getString("server");
                            long timestamp = result.getTimestamp("timestamp").getTime() / 1000;
                            BsonDocument transaction = new BsonDocument("amount", new BsonInt32(amount))
                                    .append("type", new BsonString(type)).append("source", new BsonString(source))
                                    .append("server", new BsonString(server)).append("timestamp", new BsonInt64(timestamp));
                            BsonArray array;
                            if (transactionData.containsKey(uuid)) {
                                array = transactionData.remove(uuid);
                            } else {
                                array = new BsonArray();
                            }
                            array.add(transaction);
                            transactionData.put(uuid, array);
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Transaction data loaded!");
                        player.sendMessage(ChatColor.GREEN + "Updating Mongo transaction data...");
                        int i = 1;
                        int size = transactionData.size();
                        for (Map.Entry<UUID, BsonArray> entry : transactionData.entrySet()) {
                            mongoHandler.getPlayerCollection().updateOne(Filters.eq("uuid", entry.getKey().toString()),
                                    Updates.set("transactions", entry.getValue()));
                            player.sendMessage(ChatColor.GREEN + "" + i++ + "/" + size + " Updated transaction data for " + entry.getKey().toString());
                        }
                        player.sendMessage(ChatColor.GREEN + "Finished updating Mongo transaction data!");
                        break;
                    }*/
                    /*case "autographs": {
                        player.sendMessage(ChatColor.GREEN + "Loading autograph data...");
                        HashMap<UUID, BsonArray> autographData = new HashMap<>();
                        PreparedStatement sql = connection.prepareStatement("SELECT * FROM autographs;");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            UUID uuid = UUID.fromString(result.getString("user"));
                            String author = result.getString("sender");
                            String message = result.getString("message");
                            BsonDocument autograph = new BsonDocument("author", new BsonString(author))
                                    .append("message", new BsonString(message)).append("time", new BsonInt64(System.currentTimeMillis()));
                            BsonArray array;
                            if (autographData.containsKey(uuid)) {
                                array = autographData.remove(uuid);
                            } else {
                                array = new BsonArray();
                            }
                            array.add(autograph);
                            autographData.put(uuid, array);
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Autograph data loaded!");
                        player.sendMessage(ChatColor.GREEN + "Updating Mongo autograph data...");
                        int i = 1;
                        int size = autographData.size();
                        for (Map.Entry<UUID, BsonArray> entry : autographData.entrySet()) {
                            mongoHandler.getPlayerCollection().updateOne(Filters.eq("uuid", entry.getKey().toString()),
                                    Updates.set("autographs", entry.getValue()));
                            player.sendMessage(ChatColor.GREEN + "" + i++ + "/" + size + " Updated autograph data for " + entry.getKey().toString());
                        }
                        player.sendMessage(ChatColor.GREEN + "Finished updating Mongo autograph data!");
                        break;
                    }*/
                    /*case "achievements": {
                        player.sendMessage(ChatColor.GREEN + "Loading achievement data...");
                        HashMap<UUID, BsonArray> achievementData = new HashMap<>();
                        PreparedStatement sql = connection.prepareStatement("SELECT * FROM achievements;");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            UUID uuid = UUID.fromString(result.getString("uuid"));
                            int id = result.getInt("achid");
                            long time = result.getLong("time");
                            BsonDocument achievement = new BsonDocument("id", new BsonInt32(id)).append("time", new BsonInt64(time));
                            BsonArray array;
                            if (achievementData.containsKey(uuid)) {
                                array = achievementData.remove(uuid);
                            } else {
                                array = new BsonArray();
                            }
                            array.add(achievement);
                            achievementData.put(uuid, array);
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Achievement data loaded!");
                        player.sendMessage(ChatColor.GREEN + "Updating Mongo achievement data...");
                        int i = 1;
                        int size = achievementData.size();
                        for (Map.Entry<UUID, BsonArray> entry : achievementData.entrySet()) {
                            mongoHandler.getPlayerCollection().updateOne(Filters.eq("uuid", entry.getKey().toString()),
                                    Updates.set("achievements", entry.getValue()));
                            player.sendMessage(ChatColor.GREEN + "" + i++ + "/" + size + " Updated achievement data for " + entry.getKey().toString());
                        }
                        player.sendMessage(ChatColor.GREEN + "Finished updating Mongo achievement data!");
                        break;
                    }*/
                    /*case "ridecounter": {
                        player.sendMessage(ChatColor.GREEN + "Loading ride counter data...");
                        HashMap<UUID, BsonArray> rideData = new HashMap<>();
                        PreparedStatement sql = connection.prepareStatement("SELECT * FROM ride_counter;");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            UUID uuid = UUID.fromString(result.getString("uuid"));
                            String rideName = result.getString("name");
                            String server = result.getString("server");
                            long time = result.getLong("time");
                            BsonDocument ride = new BsonDocument("name", new BsonString(rideName))
                                    .append("server", new BsonString(server))
                                    .append("time", new BsonInt64(time));
                            BsonArray array;
                            if (rideData.containsKey(uuid)) {
                                array = rideData.remove(uuid);
                            } else {
                                array = new BsonArray();
                            }
                            array.add(ride);
                            rideData.put(uuid, array);
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Ride counter data loaded!");
                        player.sendMessage(ChatColor.GREEN + "Updating Mongo ride counter data...");
                        int i = 1;
                        int size = rideData.size();
                        for (Map.Entry<UUID, BsonArray> entry : rideData.entrySet()) {
                            mongoHandler.getPlayerCollection().updateOne(Filters.eq("uuid", entry.getKey().toString()),
                                    Updates.set("parks.rides", entry.getValue()));
                            player.sendMessage(ChatColor.GREEN + "" + i++ + "/" + size + " Updated ride counter data for " + entry.getKey().toString());
                        }
                        player.sendMessage(ChatColor.GREEN + "Finished updating Mongo ride counter data!");
                        break;
                    }*/
                    /*case "inventories": {
                        player.sendMessage(ChatColor.GREEN + "Loading storage data...");
                        HashMap<UUID, InventoryCache> storageData = new HashMap<>();
                        PreparedStatement sql = connection.prepareStatement("SELECT * FROM storage2 WHERE uuid='9ab3b4c4-71d8-47c9-9e7d-adf040c53d2b' GROUP BY resort,uuid;");
                        ResultSet result = sql.executeQuery();
                        while (result.next()) {
                            UUID uuid = UUID.fromString(result.getString("uuid"));
                            String backpack = result.getString("pack");
                            String locker = result.getString("locker");
                            String hotbar = result.getString("hotbar");
                            Resort resort = Resort.fromId(result.getInt("resort"));
                            InventoryCache cache;
                            if (storageData.containsKey(uuid)) {
                                cache = storageData.get(uuid);
                            } else {
                                cache = new InventoryCache(uuid, new HashMap<>());
                            }
                            ResortInventory inv = new ResortInventory(resort, backpack, "", "",
                                    result.getInt("packsize"), locker, "", "",
                                    result.getInt("lockersize"), hotbar, "", "");
                            cache.setInventory(resort, inv);
                            storageData.put(uuid, cache);
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Storage data loaded!");
                        player.sendMessage(ChatColor.GREEN + "Updating Mongo storage data...");
                        int i = 1;
                        int size = storageData.size();
                        for (Map.Entry<UUID, InventoryCache> entry : storageData.entrySet()) {
                            for (ResortInventory inv : entry.getValue().getResorts().values()) {
                                mongoHandler.setInventoryData(entry.getKey(), inv, true);
                            }
                            player.sendMessage(ChatColor.GREEN + "" + i++ + "/" + size + " Updated inventory for " + entry.getKey().toString());
                        }
                        player.sendMessage(ChatColor.GREEN + "Finished updating Mongo storage data!");
                        break;
                    }*/
                    case "chat": {
                        int min = Integer.parseInt(args[1]);
                        int max = Integer.parseInt(args[2]);

                        String query = "SELECT * FROM `chat` LIMIT " + min + "," + max + ";";
                        player.sendMessage(ChatColor.GREEN + "Loading chat data...");
                        player.sendMessage(query);
//                        HashMap<UUID, List<Document>> messagesData = new HashMap<>();

                        List<ChatMessage> messages = new ArrayList<>();

                        List<Document> docs = new ArrayList<>();

                        player.sendMessage(ChatColor.GREEN + "Executing query...");
                        PreparedStatement sql = connection.prepareStatement(query);
                        ResultSet result = sql.executeQuery();
                        player.sendMessage(ChatColor.GREEN + "Looping through results...");
                        while (result.next()) {
                            UUID uuid;
                            try {
                                uuid = UUID.fromString(result.getString("user"));
                            } catch (Exception e) {
                                continue;
                            }
                            String message = result.getString("message");
                            long time = result.getTimestamp("timestamp").getTime() / 1000;
                            docs.add(new Document("uuid", uuid.toString()).append("message", message).append("time", time));
//                            messages.add(new ChatMessage(uuid, message, time));
//                            List<Document> list;
//                            if (messagesData.containsKey(uuid)) {
//                                list = messagesData.get(uuid);
//                            } else {
//                                list = new ArrayList<>();
//                            }
//                            list.add(new Document("message", message).append("time", time));
//                            messagesData.put(uuid, list);
                        }
                        result.close();
                        sql.close();
                        player.sendMessage(ChatColor.GREEN + "Finished loading chat data!");
                        long t = System.currentTimeMillis();
                        int i = 1;
                        double size = docs.size();
                        player.sendMessage(ChatColor.GREEN + "Now inserting into database... (" + size + " entries)");

                        mongoHandler.getChatCollection().insertMany(docs);

//                        for (ChatMessage msg : messages) {
//                            mongoHandler.logChat(msg);
//
//                            if (i++ % 500 == 0) {
//                                player.sendMessage(ChatColor.GREEN + "" + (i - 1) + "/" + size);
//                            }
//                        }

//                        for (Map.Entry<UUID, List<Document>> entry : messagesData.entrySet()) {
//                            UUID uuid = entry.getKey();
//                            List<Document> list = entry.getValue();
//                            if (list.isEmpty()) continue;
//                            BsonArray array = new BsonArray();
//                            while (!list.isEmpty()) {
//                                Document doc = list.remove(0);
//                                array.add(new BsonDocument("message", new BsonString(doc.getString("message"))).append("time", new BsonInt64(doc.getLong("time"))));
//                            }
//                            mongoHandler.getChatCollection().updateOne(MongoHandler.MongoFilter.UUID.getFilter(uuid.toString()),
//                                    Updates.pushEach("messages", array), new UpdateOptions().upsert(true));
//                            player.sendMessage(ChatColor.GREEN + "" + (((int) ((i++ / size) * 10000)) / 100.0) + "% done");
//                                mongoHandler.getChatCollection().insertOne(new Document("uuid", uuid.toString()).append("messages", list));
//                        }
                        player.sendMessage(ChatColor.GREEN + "Finished inserting into Mongo Database in " + (System.currentTimeMillis() - t) + "ms");

                        /*int size = Integer.parseInt(args[1]);
                        int increments = Integer.parseInt(args[2]);

                        int times = (int) roundUp(size, increments);
                        for (int i = 0; i < times; i++) {
                            HashMap<UUID, List<Document>> messagesData = new HashMap<>();
                            Connection sqlConnection = sqlUtil.getConnection().get();
                            try {
                                int startAt = i * increments;

                                player.sendMessage(ChatColor.GREEN + "Loading chat data trial " + i + "... (will take a while)");
                                String query = "SELECT * FROM `chat` LIMIT " + startAt + "," + increments;
//                                String query = "SELECT * FROM chat";
                                player.sendMessage(query);
                                PreparedStatement sql = sqlConnection.prepareStatement(query);
                                player.sendMessage(ChatColor.GREEN + "Executing query...");
                                ResultSet result = sql.executeQuery();
                                player.sendMessage(ChatColor.GREEN + "Looping through results...");
                                while (result.next()) {
                                    UUID uuid;
                                    try {
                                        uuid = UUID.fromString(result.getString("user"));
                                    } catch (Exception e) {
                                        continue;
                                    }
                                    String message = result.getString("message");
                                    long time = result.getTimestamp("timestamp").getTime() / 1000;
                                    List<Document> list;
                                    if (messagesData.containsKey(uuid)) {
                                        list = messagesData.get(uuid);
                                    } else {
                                        list = new ArrayList<>();
                                    }
                                    list.add(new Document("message", message).append("time", time));
                                    messagesData.put(uuid, list);
                                }
                                result.close();
                                sql.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            sqlConnection.close();
                            player.sendMessage(ChatColor.GREEN + "Finished loading chat data!");
                            long t = System.currentTimeMillis();
                            player.sendMessage(ChatColor.GREEN + "Now inserting into database... (" + messagesData.size() + " entries)");
                            for (Map.Entry<UUID, List<Document>> entry : messagesData.entrySet()) {
                                UUID uuid = entry.getKey();
                                List<Document> list = entry.getValue();
                                if (list.isEmpty()) continue;
                                BsonArray array = new BsonArray();
                                while (!list.isEmpty()) {
                                    Document doc = list.remove(0);
                                    array.add(new BsonDocument("message", new BsonString(doc.getString("message"))).append("time", new BsonInt64(doc.getLong("time"))));
                                }
                                mongoHandler.getChatCollection().updateOne(MongoHandler.MongoFilter.UUID.getFilter(uuid.toString()),
                                        Updates.pushEach("messages", array), new UpdateOptions().upsert(true));
//                                mongoHandler.getChatCollection().insertOne(new Document("uuid", uuid.toString()).append("messages", list));
                            }
                            player.sendMessage(ChatColor.GREEN + "Finished inserting into Mongo Database in " + (System.currentTimeMillis() - t) + "ms");
                        }
                        player.sendMessage(ChatColor.GREEN + "All done!");*/
                        /*player.sendMessage(ChatColor.GREEN + "Loading user data...");
                        List<UUID> players = new ArrayList<>();
                        PreparedStatement playerSql = connection.prepareStatement("SELECT user FROM chat GROUP BY user");
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
                            mongoHandler.getChatCollection().updateOne(MongoHandler.MongoFilter.UUID.getFilter(uuid.toString()),
                                    Updates.pushEach("messages", messages), new UpdateOptions().upsert(true));
                            player.sendMessage(ChatColor.GREEN + "Finished " + uuid.toString() + " (" + messages.size() + " messages)");
                        }
                        player.sendMessage(ChatColor.GREEN + "Finished processing players!");*/
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
                        int min = Integer.parseInt(args[1]);
                        int max = Integer.parseInt(args[2]);
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

                        player.sendMessage(ChatColor.GREEN + "Loading ride counter data...");
                        HashMap<UUID, BsonArray> rideData = new HashMap<>();
                        PreparedStatement rideSql = connection.prepareStatement("SELECT * FROM ride_counter;");
                        ResultSet rideResult = rideSql.executeQuery();
                        while (rideResult.next()) {
                            UUID uuid = UUID.fromString(rideResult.getString("uuid"));
                            String rideName = rideResult.getString("name");
                            String server = rideResult.getString("server");
                            long time = rideResult.getLong("time");
                            BsonDocument ride = new BsonDocument("name", new BsonString(rideName))
                                    .append("server", new BsonString(server))
                                    .append("time", new BsonInt64(time));
                            BsonArray array;
                            if (rideData.containsKey(uuid)) {
                                array = rideData.remove(uuid);
                            } else {
                                array = new BsonArray();
                            }
                            array.add(ride);
                            rideData.put(uuid, array);
                        }
                        rideResult.close();
                        rideSql.close();
                        player.sendMessage(ChatColor.GREEN + "Ride counter data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading autograph data...");
                        HashMap<UUID, BsonArray> autographData = new HashMap<>();
                        PreparedStatement autoSql = connection.prepareStatement("SELECT * FROM autographs;");
                        ResultSet autoResult = autoSql.executeQuery();
                        while (autoResult.next()) {
                            UUID uuid = UUID.fromString(autoResult.getString("user"));
                            String author = autoResult.getString("sender");
                            String message = autoResult.getString("message");
                            BsonDocument autograph = new BsonDocument("author", new BsonString(author))
                                    .append("message", new BsonString(message)).append("time", new BsonInt64(System.currentTimeMillis()));
                            BsonArray array;
                            if (autographData.containsKey(uuid)) {
                                array = autographData.remove(uuid);
                            } else {
                                array = new BsonArray();
                            }
                            array.add(autograph);
                            autographData.put(uuid, array);
                        }
                        autoResult.close();
                        autoSql.close();
                        player.sendMessage(ChatColor.GREEN + "Autograph data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading achievement data...");
                        HashMap<UUID, BsonArray> achievementData = new HashMap<>();
                        PreparedStatement achievementSql = connection.prepareStatement("SELECT * FROM achievements;");
                        ResultSet achievementResult = achievementSql.executeQuery();
                        while (achievementResult.next()) {
                            UUID uuid = UUID.fromString(achievementResult.getString("uuid"));
                            int id = achievementResult.getInt("achid");
                            long time = achievementResult.getLong("time");
                            BsonDocument achievement = new BsonDocument("id", new BsonInt32(id)).append("time", new BsonInt64(time));
                            BsonArray array;
                            if (achievementData.containsKey(uuid)) {
                                array = achievementData.remove(uuid);
                            } else {
                                array = new BsonArray();
                            }
                            array.add(achievement);
                            achievementData.put(uuid, array);
                        }
                        achievementResult.close();
                        achievementSql.close();
                        player.sendMessage(ChatColor.GREEN + "Achievement data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading transaction data...");
                        HashMap<UUID, BsonArray> transactionData = new HashMap<>();
                        PreparedStatement ecoSql = connection.prepareStatement("SELECT * FROM economy_logs;");
                        ResultSet ecoResult = ecoSql.executeQuery();
                        while (ecoResult.next()) {
                            UUID uuid = UUID.fromString(ecoResult.getString("uuid"));
                            int amount = ecoResult.getInt("amount");
                            String type = ecoResult.getString("type");
                            String source = ecoResult.getString("source");
                            String server = ecoResult.getString("server");
                            long timestamp = ecoResult.getTimestamp("timestamp").getTime() / 1000;
                            BsonDocument transaction = new BsonDocument("amount", new BsonInt32(amount))
                                    .append("type", new BsonString(type)).append("source", new BsonString(source))
                                    .append("server", new BsonString(server)).append("timestamp", new BsonInt64(timestamp));
                            BsonArray array;
                            if (transactionData.containsKey(uuid)) {
                                array = transactionData.remove(uuid);
                            } else {
                                array = new BsonArray();
                            }
                            array.add(transaction);
                            transactionData.put(uuid, array);
                        }
                        ecoResult.close();
                        ecoSql.close();
                        player.sendMessage(ChatColor.GREEN + "Transaction data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading storage data...");
                        HashMap<UUID, InventoryCache> storageData = new HashMap<>();
                        PreparedStatement storageSql = connection.prepareStatement("SELECT * FROM storage2 WHERE uuid='9ab3b4c4-71d8-47c9-9e7d-adf040c53d2b' GROUP BY resort,uuid;");
                        ResultSet storageResult = storageSql.executeQuery();
                        while (storageResult.next()) {
                            UUID uuid = UUID.fromString(storageResult.getString("uuid"));
                            String backpack = storageResult.getString("pack");
                            String locker = storageResult.getString("locker");
                            String hotbar = storageResult.getString("hotbar");
                            Resort resort = Resort.fromId(storageResult.getInt("resort"));
                            InventoryCache cache;
                            if (storageData.containsKey(uuid)) {
                                cache = storageData.get(uuid);
                            } else {
                                cache = new InventoryCache(uuid, new HashMap<>());
                            }
                            ResortInventory inv = new ResortInventory(resort, backpack, "", "",
                                    storageResult.getInt("packsize"), locker, "", "",
                                    storageResult.getInt("lockersize"), hotbar, "", "");
                            cache.setInventory(resort, inv);
                            storageData.put(uuid, cache);
                        }
                        storageResult.close();
                        storageSql.close();
                        player.sendMessage(ChatColor.GREEN + "Storage data loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading staff passwords...");
                        HashMap<UUID, String> passwordData = new HashMap<>();
                        PreparedStatement passwordSql = connection.prepareStatement("SELECT * FROM staffpasswords;");
                        ResultSet passwordResult = passwordSql.executeQuery();
                        while (passwordResult.next()) {
                            UUID uuid = UUID.fromString(passwordResult.getString("uuid"));
                            String hash = passwordResult.getString("password");
                            passwordData.put(uuid, hash);
                        }
                        passwordResult.close();
                        passwordSql.close();
                        player.sendMessage(ChatColor.GREEN + "Staff passwords loaded!");

                        player.sendMessage(ChatColor.GREEN + "Loading discord data...");
                        HashMap<UUID, String> discordData = new HashMap<>();
                        PreparedStatement discordSql = connection.prepareStatement("SELECT * FROM discord;");
                        ResultSet discordResult = discordSql.executeQuery();
                        while (discordResult.next()) {
                            UUID uuid = UUID.fromString(discordResult.getString("minecraftUUID"));
                            String username = discordResult.getString("discordUsername");
                            discordData.put(uuid, username);
                        }
                        discordResult.close();
                        discordSql.close();
                        player.sendMessage(ChatColor.GREEN + "Discord data loaded!");

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

                        player.sendMessage(ChatColor.GREEN + "Processing players...");
                        PreparedStatement playerSql = connection.prepareStatement("SELECT * FROM player_data WHERE ipAddress!='no ip' GROUP BY uuid ORDER BY id ASC LIMIT " + min + "," + max + ";");
                        ResultSet playerResult = playerSql.executeQuery();
                        List<Document> documents = new ArrayList<>();
                        int id = 0;
                        while (playerResult.next()) {
                            UUID uuid = UUID.fromString(playerResult.getString("uuid"));
                            String username = playerResult.getString("username");
                            Rank rank = Rank.fromString(playerResult.getString("rank"));
                            dashboard.getLogger().info("Starting processing " + username + "...");
//                            player.sendMessage(ChatColor.GREEN + "Starting processing " + username + "...");
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
                            playerDocument.put("onlineTime", (long) playerResult.getInt("onlinetime"));
                            if (discordData.containsKey(uuid)) {
                                playerDocument.put("discordUsername", discordData.remove(uuid));
                            }
                            if (rank.getRankId() >= Rank.TRAINEE.getRankId() && passwordData.containsKey(uuid)) {
                                playerDocument.put("staffPassword", passwordData.remove(uuid));
                            }

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

                            InventoryCache cache;
                            if (storageData.containsKey(uuid)) {
                                cache = storageData.remove(uuid);
                            } else {
                                cache = new InventoryCache(uuid, new HashMap<>());
                            }

                            List<Object> inventoryData = new ArrayList<>();

                            for (ResortInventory inv : cache.getResorts().values()) {
                                UpdateData data = InventoryUtil.getDataFromJson(inv.getBackpackJSON(), inv.getBackpackSize(),
                                        inv.getLockerJSON(), inv.getLockerSize(), inv.getHotbarJSON());
                                Document doc = new Document("packcontents", data.getPack()).append("packsize", data.getPackSize())
                                        .append("lockercontents", data.getLocker()).append("lockersize", data.getLockerSize())
                                        .append("hotbarcontents", data.getHotbar()).append("resort", inv.getResort().getId());
                                inventoryData.add(doc);
                            }

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

                            BsonArray rideArray;

                            if (rideData.containsKey(uuid)) {
                                rideArray = rideData.remove(uuid);
                            } else {
                                rideArray = new BsonArray();
                            }

                            parkData.put("rides", rideArray);

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

                            BsonArray achievementArray;

                            if (achievementData.containsKey(uuid)) {
                                achievementArray = achievementData.remove(uuid);
                            } else {
                                achievementArray = new BsonArray();
                            }

                            playerDocument.put("achievements", achievementArray);

                            BsonArray autographArray;

                            if (autographData.containsKey(uuid)) {
                                autographArray = autographData.remove(uuid);
                            } else {
                                autographArray = new BsonArray();
                            }

                            playerDocument.put("autographs", autographArray);

                            BsonArray transactionArray;

                            if (transactionData.containsKey(uuid)) {
                                transactionArray = transactionData.remove(uuid);
                            } else {
                                transactionArray = new BsonArray();
                            }

                            playerDocument.put("transactions", transactionArray);

                            List<Object> ignoring = new ArrayList<>();
                            for (IgnoreData data : ignoreList) {
                                if (!data.getUuid().equals(uuid)) continue;
                                Document ignoreDoc = new Document("uuid", data.getIgnored().toString())
                                        .append("started", data.getStarted());
                            }
                            playerDocument.put("ignoring", ignoring);

                            documents.add(playerDocument);
                            if (id % 500 == 0) {
                                player.sendMessage(ChatColor.GREEN + "" + id);
                            }
                            dashboard.getLogger().info((++id) + " Finished " + username);

//                            player.sendMessage(ChatColor.GREEN + "Requesting past usernames...");
//                            mongoHandler.updatePreviousUsernames(uuid, username);
//                            player.sendMessage(ChatColor.GREEN + "Past usernames updated!");
                        }
                        playerResult.close();
                        playerSql.close();
                        player.sendMessage(ChatColor.GREEN + "Inserting into database...");
                        long t = System.currentTimeMillis();
                        mongoHandler.getPlayerCollection().insertMany(documents);
                        player.sendMessage(ChatColor.GREEN + "Inserted into database in " + (System.currentTimeMillis() - t) + "ms");

                        ignoreList.clear();
                        banList.clear();
                        kickList.clear();
                        muteList.clear();
                        rideData.clear();
                        autographData.clear();
                        achievementData.clear();
                        transactionData.clear();
                        storageData.clear();
                        passwordData.clear();
                        discordData.clear();
                        cosmeticsMap.clear();
                        outfitMap.clear();

                        System.gc();
                        break;
                    }
                }
            } catch (Exception e) {
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
