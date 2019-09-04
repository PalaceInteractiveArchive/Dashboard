package network.palace.dashboard.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.InventoryCache;
import network.palace.dashboard.handlers.InventoryUpdate;
import network.palace.dashboard.handlers.ResortInventory;
import network.palace.dashboard.handlers.UpdateData;
import network.palace.dashboard.packets.inventory.PacketInventoryContent;
import network.palace.dashboard.packets.inventory.Resort;
import org.bson.*;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author Innectic
 * @since 6/10/2017
 */
public class InventoryUtil {
    public static final int STORAGE_VERSION = 1;
    @Getter private Map<UUID, InventoryCache> cachedInventories = new HashMap<>();

    public InventoryUtil() {
        Dashboard dashboard = Launcher.getDashboard();
        File f = new File("inventories.txt");
        if (f.exists()) {
            try {
                Scanner scanner = new Scanner(new FileReader(f));
                while (scanner.hasNextLine()) {
                    String json = scanner.nextLine();
                    JsonObject o = new JsonParser().parse(json).getAsJsonObject();
                    UUID uuid = UUID.fromString(o.get("uuid").getAsString());
                    HashMap<Resort, ResortInventory> map = new HashMap<>();
                    JsonArray arr = o.get("resorts").getAsJsonArray();
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject ob = arr.get(i).getAsJsonObject();
                        Resort resort = Resort.fromId(ob.get("resort").getAsInt());
                        String packJSON;
                        String packHash;
                        String dbPackHash;
                        int packsize;
                        String lockerJSON;
                        String lockerHash;
                        String dbLockerHash;
                        int lockersize;
                        String baseJSON;
                        String baseHash;
                        String dbBaseHash;
                        String buildJSON;
                        String buildHash;
                        String dbBuildHash;

                        if (ob.get("backpackJSON").isJsonNull()) {
                            packJSON = "";
                        } else {
                            packJSON = ob.get("backpackJSON").getAsString();
                        }
                        if (ob.get("backpackHash").isJsonNull()) {
                            packHash = "";
                        } else {
                            packHash = ob.get("backpackHash").getAsString();
                        }
                        if (ob.get("dbBackpackHash").isJsonNull()) {
                            dbPackHash = "";
                        } else {
                            dbPackHash = ob.get("dbBackpackHash").getAsString();
                        }
                        if (ob.get("backpacksize").isJsonNull()) {
                            packsize = -1;
                        } else {
                            packsize = ob.get("backpacksize").getAsInt();
                        }

                        if (ob.get("lockerJSON").isJsonNull()) {
                            lockerJSON = "";
                        } else {
                            lockerJSON = ob.get("lockerJSON").getAsString();
                        }
                        if (ob.get("lockerHash").isJsonNull()) {
                            lockerHash = "";
                        } else {
                            lockerHash = ob.get("lockerHash").getAsString();
                        }
                        if (ob.get("dbLockerHash").isJsonNull()) {
                            dbLockerHash = "";
                        } else {
                            dbLockerHash = ob.get("dbLockerHash").getAsString();
                        }
                        if (ob.get("lockersize").isJsonNull()) {
                            lockersize = -1;
                        } else {
                            lockersize = ob.get("lockersize").getAsInt();
                        }

                        if (ob.get("baseJSON").isJsonNull()) {
                            baseJSON = "";
                        } else {
                            baseJSON = ob.get("baseJSON").getAsString();
                        }
                        if (ob.get("baseHash").isJsonNull()) {
                            baseHash = "";
                        } else {
                            baseHash = ob.get("baseHash").getAsString();
                        }
                        if (ob.get("dbBaseHash").isJsonNull()) {
                            dbBaseHash = "";
                        } else {
                            dbBaseHash = ob.get("dbBaseHash").getAsString();
                        }

                        if (ob.get("buildJSON").isJsonNull()) {
                            buildJSON = "";
                        } else {
                            buildJSON = ob.get("buildJSON").getAsString();
                        }
                        if (ob.get("buildHash").isJsonNull()) {
                            buildHash = "";
                        } else {
                            buildHash = ob.get("buildHash").getAsString();
                        }
                        if (ob.get("dbBuildHash").isJsonNull()) {
                            dbBuildHash = "";
                        } else {
                            dbBuildHash = ob.get("dbBuildHash").getAsString();
                        }

                        map.put(resort, new ResortInventory(resort, packJSON, packHash, dbPackHash, packsize, lockerJSON,
                                lockerHash, dbLockerHash, lockersize, baseJSON, baseHash, dbBaseHash, buildJSON, buildHash, dbBuildHash));
                    }
                    InventoryCache cache = new InventoryCache(uuid, map);
                    cachedInventories.put(uuid, cache);
                }
            } catch (Exception e) {
                dashboard.getLogger().error("An exception occurred while parsing inventories.txt - " + e.getMessage());
                e.printStackTrace();
            }
            f.delete();
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (InventoryCache cache : new ArrayList<>(cachedInventories.values())) {
                    if (cache == null || cache.getResorts() == null) {
                        continue;
                    }
                    InventoryUpdate update = new InventoryUpdate();
                    for (ResortInventory inv : cache.getResorts().values()) {
                        if (inv == null) {
                            continue;
                        }
                        if (!inv.getDbBackpackHash().equals(inv.getBackpackHash()) ||
                                !inv.getDbLockerHash().equals(inv.getLockerHash()) ||
                                !inv.getDbBaseHash().equals(inv.getBaseHash()) ||
                                !inv.getDbBuildHash().equals(inv.getBuildHash())) {

                            String backpackJSON = inv.getBackpackJSON();
                            int packSize = inv.getBackpackSize();
                            String lockerJSON = inv.getLockerJSON();
                            int lockerSize = inv.getLockerSize();
                            String baseJSON = inv.getBaseJSON();
                            String buildJSON = inv.getBuildJSON();

                            UpdateData data = getDataFromJson(backpackJSON, packSize, lockerJSON, lockerSize, baseJSON, buildJSON);
                            update.setData(inv.getResort(), data);

                            inv.setDbBackpackHash(inv.getBackpackHash());
                            inv.setDbLockerHash(inv.getLockerHash());
                            inv.setDbBaseHash(inv.getBaseHash());
                            inv.setDbBuildHash(inv.getBuildHash());
                        }
                    }
                    boolean updated = false;
                    Runnable runnable = () -> updateData(cache.getUuid(), update);
                    if (update.shouldUpdate()) {
                        updated = true;
                        dashboard.getSchedulerManager().runAsync(runnable);
                    }
                    if (dashboard.getPlayer(cache.getUuid()) == null) {
                        cachedInventories.remove(cache.getUuid());
                        if (!updated) {
                            dashboard.getSchedulerManager().runAsync(runnable);
                        }
                    }
                }
            }
        }, 1000, 10000);
    }

    /**
     * Create an UpdateData object based on the provided inventory JSON
     *
     * @param backpackJSON the backpack JSON
     * @param backpackSize the backpack size
     * @param lockerJSON   the locker JSON
     * @param lockerSize   the locker size
     * @param baseJSON     the base JSON
     * @param buildJSON    the build JSON
     * @return an UpdateData object based on the provided inventory JSON
     */
    public static UpdateData getDataFromJson(String backpackJSON, int backpackSize, String lockerJSON, int lockerSize, String baseJSON, String buildJSON) {
        BsonArray pack = jsonToArray(backpackJSON);
        BsonArray locker = jsonToArray(lockerJSON);
        BsonArray base = jsonToArray(baseJSON);
        BsonArray build = jsonToArray(buildJSON);

        return new UpdateData(pack, backpackSize, locker, lockerSize, base, build);
    }

    /**
     * Create a BsonArray from the provided JSON string
     *
     * @param json the JSON string
     * @return the BsonArray
     */
    public static BsonArray jsonToArray(String json) {
        BsonArray array = new BsonArray();
        if (json == null || json.isEmpty()) return array;
        JsonElement element = new JsonParser().parse(json);
        if (element.isJsonArray()) {
            JsonArray baseArray = element.getAsJsonArray();

            int i = 0;
            for (JsonElement e2 : baseArray) {
                JsonObject o = e2.getAsJsonObject();
                BsonDocument item = InventoryUtil.getBsonFromJson(o.toString());
                array.add(item);
                i++;
            }
        }
        return array;
    }

    /**
     * Cache a player's inventory in Dashboard, or update an existing cache of the player's inventory
     *
     * @param uuid   the uuid of the player to cache
     * @param packet A packet containing the player's inventory data
     */
    public void cacheInventory(UUID uuid, PacketInventoryContent packet) {
        if (cachedInventories.containsKey(uuid)) {
            //If a cache exists, update its data with the new data
            ResortInventory cache = cachedInventories.get(uuid).getResorts().get(packet.getResort());
            if (cache == null) {
                return;
            }
            ResortInventory inv = new ResortInventory();
            inv.setResort(packet.getResort());

            if (packet.getBackpackHash().equals("")) {
                inv.setBackpackHash(cache.getBackpackHash());
                inv.setBackpackJSON(cache.getBackpackJSON());
                inv.setDbBackpackHash(cache.getDbBackpackHash());
            } else {
                inv.setBackpackHash(packet.getBackpackHash());
                inv.setBackpackJSON(packet.getBackpackJson());
                inv.setDbBackpackHash("");
            }

            if (packet.getBackpackSize() == -1) {
                inv.setBackpackSize(cache.getBackpackSize());
            } else {
                inv.setBackpackSize(packet.getBackpackSize());
            }

            if (packet.getLockerHash().equals("")) {
                inv.setLockerHash(cache.getLockerHash());
                inv.setLockerJSON(cache.getLockerJSON());
                inv.setDbLockerHash(cache.getDbLockerHash());
            } else {
                inv.setLockerHash(packet.getLockerHash());
                inv.setLockerJSON(packet.getLockerJson());
                inv.setDbLockerHash("");
            }

            if (packet.getLockerSize() == -1) {
                inv.setLockerSize(cache.getLockerSize());
            } else {
                inv.setLockerSize(packet.getLockerSize());
            }

            if (packet.getBaseHash().equals("")) {
                inv.setBaseHash(cache.getBaseHash());
                inv.setBaseJSON(cache.getBaseJSON());
                inv.setDbBaseHash(cache.getDbBaseHash());
            } else {
                inv.setBaseHash(packet.getBaseHash());
                inv.setBaseJSON(packet.getBaseJson());
                inv.setDbBaseHash("");
            }

            if (packet.getBuildHash().equals("")) {
                inv.setBuildHash(cache.getBuildHash());
                inv.setBuildJSON(cache.getBuildJSON());
                inv.setDbBuildHash(cache.getDbBuildHash());
            } else {
                inv.setBuildHash(packet.getBuildHash());
                inv.setBuildJSON(packet.getBuildJson());
                inv.setDbBuildHash("");
            }

            cachedInventories.get(uuid).setInventory(packet.getResort(), inv);
        } else {
            //If a cache doesn't exist, make one
            HashMap<Resort, ResortInventory> map = new HashMap<>();
            map.put(packet.getResort(), new ResortInventory(packet.getResort(), packet.getBackpackJson(), packet.getBackpackHash(),
                    "", packet.getBackpackSize(), packet.getLockerJson(), packet.getLockerHash(),
                    "", packet.getLockerSize(), packet.getBaseJson(), packet.getBaseHash(),
                    "", packet.getBuildJson(), packet.getBuildHash(), ""));
            InventoryCache cache = new InventoryCache(uuid, map);
            cachedInventories.put(uuid, cache);
            fillMapAsync(uuid);
            //Fill the new cache with other resort data from the database if it exists
        }
    }

    /**
     * Asynchronously fill a player's InventoryCache with missing resort data from the database
     *
     * @param uuid the uuid of the player's cache to fill
     */
    private void fillMapAsync(UUID uuid) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            InventoryCache cache = cachedInventories.get(uuid);

            if (cache == null) cache = new InventoryCache(uuid, new HashMap<>());

            HashMap<Resort, ResortInventory> resorts = cache.getResorts();
            boolean changed = false;

            for (Resort resort : Resort.values()) {
                if (resorts.containsKey(resort)) continue;
                //A cache entry already exists for that resort, so skip

                changed = true;
                ResortInventory inv = getResortInventoryFromDatabase(uuid, resort);
                if (inv.isEmpty()) continue;
                //The database entry is empty, so don't cache it (could be a bad entry)

                cache.setInventory(resort, inv);
            }
            if (!changed) return;
            //Only update if any changes were actually made
            cachedInventories.put(uuid, cache);
        });
    }

    /**
     * Get the inventory for a player
     *
     * @param uuid the uuid of the player
     * @return the inventory of the player. Defaults to a blank string if none is present
     */
    public ResortInventory getInventory(UUID uuid, Resort resort) {
        try {
            InventoryCache cache = cachedInventories.get(uuid);
            if (cache == null) {
                cache = getInventoryFromDatabase(uuid);
                cachedInventories.put(uuid, cache);
            }
            ResortInventory inv = cache.getResorts().get(resort);
            if (inv == null) {
                return createResortInventory(uuid, resort);
            }
            return inv;
        } catch (Exception e) {
            e.printStackTrace();
            return new ResortInventory();
        }
    }

    private ResortInventory createResortInventory(UUID uuid, Resort resort) {
        Dashboard dashboard = Launcher.getDashboard();
        ResortInventory inv = new ResortInventory();
        inv.setResort(resort);
        dashboard.getMongoHandler().setInventoryData(uuid, inv, true);
        InventoryCache cache = cachedInventories.get(uuid);
        if (cache != null) {
            cache.setInventory(resort, inv);
            cachedInventories.put(uuid, cache);
        }
        return inv;
    }

    /**
     * Get all inventory entries from database for a specific user
     *
     * @param uuid the player's uuid
     * @return A cache with all of the player's resort inventories
     */
    private InventoryCache getInventoryFromDatabase(UUID uuid) {
        HashMap<Resort, ResortInventory> map = new HashMap<>();
        try {
            Dashboard dashboard = Launcher.getDashboard();
            Document invData = dashboard.getMongoHandler().getParkInventoryData(uuid);

            boolean clearUnversionedInventories = false;

            if (invData == null) return new InventoryCache(uuid, map);

            for (Object o : invData.get("storage", ArrayList.class)) {
                Document inv = (Document) o;
                if (!inv.containsKey("version")) {
                    System.out.println("UNVERSIONED STORAGE FOUND");
                    clearUnversionedInventories = true;
                    continue;
                }
                int version = inv.getInteger("version");
                int resortID = inv.getInteger("resort");
                Resort resort = Resort.fromId(resortID);
                if (version != STORAGE_VERSION) {
                    System.out.println("INCORRECT STORAGE VERSION FOUND");
                    continue;
                }
                StringBuilder backpack = new StringBuilder("[");
                ArrayList packcontents = inv.get("backpack", ArrayList.class);
                for (int i = 0; i < packcontents.size(); i++) {
                    Document item = (Document) packcontents.get(i);
                    if (!item.containsKey("amount") || !(item.get("amount") instanceof Integer)) {
                        backpack.append("{}");
                    } else {
                        backpack.append("{type:'").append(item.getString("type"))
                                .append("',data:").append(item.getInteger("data"))
                                .append(",amount:").append(item.getInteger("amount"))
                                .append(",tag:'").append(item.getString("tag")).append("'}");
                    }
                    if (i < (packcontents.size() - 1)) {
                        backpack.append(",");
                    }
                }
                backpack.append("]");
                StringBuilder locker = new StringBuilder("[");
                ArrayList lockercontents = inv.get("locker", ArrayList.class);
                for (int i = 0; i < lockercontents.size(); i++) {
                    Document item = (Document) lockercontents.get(i);
                    if (!item.containsKey("amount") || !(item.get("amount") instanceof Integer)) {
                        locker.append("{}");
                    } else {
                        locker.append("{type:'").append(item.getString("type"))
                                .append("',data:").append(item.getInteger("data"))
                                .append(",amount:").append(item.getInteger("amount"))
                                .append(",tag:'").append(item.getString("tag")).append("'}");
                    }
                    if (i < (lockercontents.size() - 1)) {
                        locker.append(",");
                    }
                }
                locker.append("]");
                StringBuilder base = new StringBuilder("[");
                ArrayList basecontents = inv.get("base", ArrayList.class);
                for (int i = 0; i < basecontents.size(); i++) {
                    Document item = (Document) basecontents.get(i);
                    if (!item.containsKey("amount") || !(item.get("amount") instanceof Integer)) {
                        base.append("{}");
                    } else {
                        base.append("{type:'").append(item.getString("type"))
                                .append("',data:").append(item.getInteger("data"))
                                .append(",amount:").append(item.getInteger("amount"))
                                .append(",tag:'").append(item.getString("tag")).append("'}");
                    }
                    if (i < (basecontents.size() - 1)) {
                        base.append(",");
                    }
                }
                base.append("]");
                StringBuilder build = new StringBuilder("[");
                ArrayList buildcontents = inv.get("build", ArrayList.class);
                for (int i = 0; i < buildcontents.size(); i++) {
                    Document item = (Document) buildcontents.get(i);
                    if (!item.containsKey("amount") || !(item.get("amount") instanceof Integer)) {
                        build.append("{}");
                    } else {
                        build.append("{type:'").append(item.getString("type"))
                                .append("',data:").append(item.getInteger("data"))
                                .append(",amount:").append(item.getInteger("amount"))
                                .append(",tag:'").append(item.getString("tag")).append("'}");
                    }
                    if (i < (buildcontents.size() - 1)) {
                        build.append(",");
                    }
                }
                build.append("]");
                int backpacksize = inv.getInteger("backpacksize");
                int lockersize = inv.getInteger("lockersize");
                ResortInventory resortInventory = new ResortInventory(resort, backpack.toString(), generateHash(backpack.toString()), "", backpacksize,
                        locker.toString(), generateHash(locker.toString()), "", lockersize,
                        base.toString(), generateHash(base.toString()), "",
                        build.toString(), generateHash(build.toString()), "");
                map.put(resort, resortInventory);
            }
            if (clearUnversionedInventories) dashboard.getMongoHandler().clearUnversionedStorage(uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new InventoryCache(uuid, map);
    }

    /**
     * Get a resort's inventory for a player
     *
     * @param uuid   the uuid of the player
     * @param resort the resort inventory to retrieve
     * @return the resort's inventory for the player
     */
    private ResortInventory getResortInventoryFromDatabase(UUID uuid, Resort resort) {
        Dashboard dashboard = Launcher.getDashboard();
        Document doc = dashboard.getMongoHandler().getParkInventory(uuid, resort);

        BsonArray pack = doc.get("backpack", BsonArray.class);
        int backpackSize = doc.getInteger("backpacksize");
        BsonArray locker = doc.get("locker", BsonArray.class);
        int lockerSize = doc.getInteger("lockersize");
        BsonArray base = doc.get("base", BsonArray.class);
        BsonArray build = doc.get("build", BsonArray.class);

        String backpackJSON = pack.toString();
        String lockerJSON = locker.toString();
        String baseJSON = base.toString();
        String buildJSON = build.toString();

        return new ResortInventory(resort, backpackJSON, generateHash(backpackJSON), "", backpackSize,
                lockerJSON, generateHash(lockerJSON), "", lockerSize,
                baseJSON, generateHash(baseJSON), "",
                buildJSON, generateHash(buildJSON), "");
    }

    /**
     * Generate hash for inventory JSON
     *
     * @param inventory the JSON
     * @return MD5 hash of inventory
     */
    private String generateHash(String inventory) {
        if (inventory == null) {
            inventory = "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(inventory.getBytes());
            return DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("NO MD5?");
            return "null";
        }
    }

    /**
     * Update player inventory data in the database
     *
     * @param uuid   player's uuid
     * @param update class containing all values to change
     */
    private void updateData(UUID uuid, InventoryUpdate update) {
        try {
            Launcher.getDashboard().getMongoHandler().updateInventoryData(uuid, update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert a JSON string to a Mongo Document
     *
     * @param json the JSON string
     * @return a Mongo document
     */
    public static Document getItemFromJson(String json) {
        JsonObject o = new JsonParser().parse(json).getAsJsonObject();
        if (!o.has("type")) {
            return new Document();
        }
        Document doc;
        try {
            doc = new Document("type", o.get("type").getAsString())
                    .append("data", o.get("data").getAsInt())
                    .append("amount", o.get("amount").getAsInt())
                    .append("tag", o.get("tag").getAsString());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return doc;
    }

    /**
     * Convert a JSON string to a BsonDocument
     *
     * @param json the JSON string
     * @return a Bsondocument
     */
    public static BsonDocument getBsonFromJson(String json) {
        JsonObject o = new JsonParser().parse(json).getAsJsonObject();
        if (!o.has("type")) {
            return new BsonDocument();
        }
        BsonDocument doc;
        try {
            doc = new BsonDocument("type", new BsonString(o.get("type").getAsString()))
                    .append("data", new BsonInt32(o.get("data").getAsInt()))
                    .append("amount", new BsonInt32(o.get("amount").getAsInt()))
                    .append("tag", o.get("tag") == null ? new BsonString("") : new BsonString(o.get("tag").getAsString()));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        return doc;
    }
}
