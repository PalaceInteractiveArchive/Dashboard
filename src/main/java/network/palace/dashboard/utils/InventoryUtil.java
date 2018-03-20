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
                        String sqlPackHash;
                        int packsize;
                        String lockerJSON;
                        String lockerHash;
                        String sqlLockerHash;
                        int lockersize;
                        String hotbarJSON;
                        String hotbarHash;
                        String sqlHotbarHash;
                        if (ob.get("packJSON").isJsonNull()) {
                            packJSON = "";
                        } else {
                            packJSON = ob.get("packJSON").getAsString();
                        }
                        if (ob.get("packHash").isJsonNull()) {
                            packHash = "";
                        } else {
                            packHash = ob.get("packHash").getAsString();
                        }
                        if (ob.get("sqlPackHash").isJsonNull()) {
                            sqlPackHash = "";
                        } else {
                            sqlPackHash = ob.get("sqlPackHash").getAsString();
                        }
                        if (ob.get("packsize").isJsonNull()) {
                            packsize = 0;
                        } else {
                            packsize = ob.get("packsize").getAsInt();
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
                        if (ob.get("sqlLockerHash").isJsonNull()) {
                            sqlLockerHash = "";
                        } else {
                            sqlLockerHash = ob.get("sqlLockerHash").getAsString();
                        }
                        if (ob.get("lockersize").isJsonNull()) {
                            lockersize = 0;
                        } else {
                            lockersize = ob.get("lockersize").getAsInt();
                        }

                        if (ob.get("hotbarJSON").isJsonNull()) {
                            hotbarJSON = "";
                        } else {
                            hotbarJSON = ob.get("hotbarJSON").getAsString();
                        }
                        if (ob.get("hotbarHash").isJsonNull()) {
                            hotbarHash = "";
                        } else {
                            hotbarHash = ob.get("hotbarHash").getAsString();
                        }
                        if (ob.get("sqlHotbarHash").isJsonNull()) {
                            sqlHotbarHash = "";
                        } else {
                            sqlHotbarHash = ob.get("sqlHotbarHash").getAsString();
                        }

                        map.put(resort, new ResortInventory(resort, packJSON, packHash, sqlPackHash, packsize, lockerJSON,
                                lockerHash, sqlLockerHash, lockersize, hotbarJSON, hotbarHash, sqlHotbarHash));
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
                        if (!inv.getSqlBackpackHash().equals(inv.getBackpackHash()) ||
                                !inv.getSqlLockerHash().equals(inv.getLockerHash()) ||
                                !inv.getSqlHotbarHash().equals(inv.getHotbarHash())) {

                            String backpackJSON = inv.getBackpackJSON();
                            int packSize = inv.getBackpackSize();
                            String lockerJSON = inv.getLockerJSON();
                            int lockerSize = inv.getLockerSize();
                            String hotbarJSON = inv.getHotbarJSON();

                            UpdateData data = getDataFromJson(backpackJSON, packSize, lockerJSON, lockerSize, hotbarJSON);
                            update.setData(inv.getResort(), data);

                            inv.setSqlBackpackHash(inv.getBackpackHash());
                            inv.setSqlLockerHash(inv.getLockerHash());
                            inv.setSqlHotbarHash(inv.getHotbarHash());
                        }
                    }
                    boolean updated = false;
                    if (update.shouldUpdate()) {
                        updated = true;
                        dashboard.getSchedulerManager().runAsync(() -> updateData(cache.getUuid(), update));
                    }
                    if (dashboard.getPlayer(cache.getUuid()) == null) {
                        cachedInventories.remove(cache.getUuid());
                        if (!updated) {
                            dashboard.getSchedulerManager().runAsync(() -> updateData(cache.getUuid(), update));
                        }
                    }
                }
            }
        }, 1000, 60000);
    }

    public static UpdateData getDataFromJson(String backpackJSON, int backpackSize, String lockerJSON, int lockerSize, String hotbarJSON) {
        BsonArray pack = jsonToArray(backpackJSON);
        BsonArray locker = jsonToArray(lockerJSON);
        BsonArray hotbar = jsonToArray(hotbarJSON);

        return new UpdateData(pack, backpackSize, locker, lockerSize, hotbar);
    }

    public static BsonArray jsonToArray(String json) {
        BsonArray array = new BsonArray();
        JsonElement element = new JsonParser().parse(json);
        if (element.isJsonArray()) {
            JsonArray hotbarArray = element.getAsJsonArray();

            int i = 0;
            for (JsonElement e2 : hotbarArray) {
                JsonObject o = e2.getAsJsonObject();
                BsonDocument item = InventoryUtil.getBsonFromJson(o.toString());
                array.add(item);
                i++;
            }
        }
        return array;
    }

    /**
     * Cache a player's inventory
     *
     * @param uuid   the uuid of the player to cache
     * @param packet A packet containing the player's inventory data
     */
    public void cacheInventory(UUID uuid, PacketInventoryContent packet) {
        if (cachedInventories.containsKey(uuid)) {
            ResortInventory cache = cachedInventories.get(uuid).getResorts().get(packet.getResort());
            if (cache == null) {
                return;
            }
            ResortInventory inv = new ResortInventory();
            inv.setResort(packet.getResort());
            if (packet.getBackpackHash().equals("")) {
                inv.setBackpackHash(cache.getBackpackHash());
                inv.setBackpackJSON(cache.getBackpackJSON());
                inv.setSqlBackpackHash(cache.getSqlBackpackHash());
            } else {
                inv.setBackpackHash(packet.getBackpackHash());
                inv.setBackpackJSON(packet.getBackpackJson());
                inv.setSqlBackpackHash("");
            }
            if (packet.getLockerHash().equals("")) {
                inv.setLockerHash(cache.getLockerHash());
                inv.setLockerJSON(cache.getLockerJSON());
                inv.setSqlLockerHash(cache.getSqlLockerHash());
            } else {
                inv.setLockerHash(packet.getLockerHash());
                inv.setLockerJSON(packet.getLockerJson());
                inv.setSqlLockerHash("");
            }
            if (packet.getHotbarHash().equals("")) {
                inv.setHotbarHash(cache.getHotbarHash());
                inv.setHotbarJSON(cache.getHotbarJSON());
                inv.setSqlHotbarHash(cache.getSqlHotbarHash());
            } else {
                inv.setHotbarHash(packet.getHotbarHash());
                inv.setHotbarJSON(packet.getHotbarJson());
                inv.setSqlHotbarHash("");
            }
            cachedInventories.get(uuid).setInventory(packet.getResort(), inv);
            return;
        }
        HashMap<Resort, ResortInventory> map = new HashMap<>();
        map.put(packet.getResort(), new ResortInventory(packet.getResort(), packet.getBackpackJson(), packet.getBackpackHash(),
                "", packet.getBackpackSize(), packet.getLockerJson(), packet.getLockerHash(),
                "", packet.getLockerSize(), packet.getHotbarJson(), packet.getHotbarHash(), ""));
        InventoryCache cache = new InventoryCache(uuid, map);
        cachedInventories.put(uuid, cache);
        fillMapAsync(uuid);
    }

    /**
     * Asynchronously fill an inventory map with missing resorts from the database
     *
     * @param uuid the uuid of the player's cache to fill
     */
    private void fillMapAsync(UUID uuid) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            InventoryCache cache = cachedInventories.get(uuid);
            if (cache == null)
                cache = new InventoryCache(uuid, new HashMap<>());
            HashMap<Resort, ResortInventory> resorts = cache.getResorts();
            boolean changed = false;
            for (Resort resort : Resort.values()) {
                if (resorts.containsKey(resort)) {
                    continue;
                }
                changed = true;
                ResortInventory inv = getResortInventoryFromDatabase(uuid, resort);
                if (inv.isEmpty())
                    continue;
                cache.setInventory(resort, inv);
            }
            if (!changed) return;
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
        InventoryCache cache = cachedInventories.get(uuid);
        if (cache == null) {
            cache = getInventoryFromDatabase(uuid);
        }
        ResortInventory inv = cache.getResorts().get(resort);
        if (inv == null) {
            return createResortInventory(uuid, resort);
        }
        return inv;
    }

    private ResortInventory createResortInventory(UUID uuid, Resort resort) {
        Dashboard dashboard = Launcher.getDashboard();
        ResortInventory inv = new ResortInventory();
        dashboard.getMongoHandler().setInventoryData(uuid, inv);
        return inv;
    }

    /**
     * Get all inventory values from database for a specific user
     *
     * @param uuid the player's uuid
     * @return A cache with all of the player's resort inventories
     */
    private InventoryCache getInventoryFromDatabase(UUID uuid) {
        HashMap<Resort, ResortInventory> map = new HashMap<>();
        Dashboard dashboard = Launcher.getDashboard();
        Document invData = dashboard.getMongoHandler().getParkInventoryData(uuid);
        for (Object o : invData.get("inventories", ArrayList.class)) {
            Document inv = (Document) o;
            int resortID = inv.getInteger("resort");
            String backpack = inv.get("packcontents", Document.class).toString();
            String locker = inv.get("lockercontents", Document.class).toString();
            String hotbar = inv.get("hotbarcontents", Document.class).toString();
            int packsize = inv.getInteger("packsize");
            int lockersize = inv.getInteger("lockrsize");
            Resort resort = Resort.fromId(resortID);
            ResortInventory resortInventory = new ResortInventory(resort, backpack, generateHash(backpack), "",
                    packsize, locker, generateHash(locker), "", lockersize, hotbar, generateHash(hotbar), "");
            map.put(resort, resortInventory);
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

        BsonArray pack = doc.get("packcontents", BsonArray.class);
        int backpackSize = doc.getInteger("packsize");
        BsonArray locker = doc.get("lockercontents", BsonArray.class);
        int lockerSize = doc.getInteger("lockersize");
        BsonArray hotbar = doc.get("hotbarcontents", BsonArray.class);

        String backpackJSON = pack.toString();
        String lockerJSON = locker.toString();
        String hotbarJSON = hotbar.toString();

        return new ResortInventory(resort, backpackJSON, generateHash(backpackJSON), "", backpackSize, lockerJSON,
                generateHash(lockerJSON), "", lockerSize, hotbarJSON, generateHash(hotbarJSON), "");
    }

    /**
     * Remove an inventory hash
     *
     * @param uuid the uuid who's hash to remove
     */
    public void removeHash(UUID uuid) {
        cachedInventories.remove(uuid);
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
     * Update player inventory data
     *
     * @param uuid   player's uuid
     * @param update class containing all values to change
     */
    private void updateData(UUID uuid, InventoryUpdate update) {
        Launcher.getDashboard().getMongoHandler().updateInventoryData(uuid, update);
    }

    /**
     * Convert a JSON string to a Mongo Document
     *
     * @param json the JSON string
     * @return a Mongo document
     */
    public static Document getItemFromJson(String json) {
        JsonObject o = new JsonParser().parse(json).getAsJsonObject();
        if (!o.has("t")) {
            return new Document();
        }
        Document doc;
        try {
            doc = new Document("a", o.get("a").getAsInt()).append("t", o.get("t").getAsInt()).append("da", o.get("da").getAsInt())
                    .append("du", o.get("du").getAsShort()).append("ta", o.get("ta").getAsString());
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
        if (!o.has("t")) {
            return new BsonDocument();
        }
        BsonDocument doc;
        try {
            doc = new BsonDocument("a", new BsonInt32(o.get("a").getAsInt())).append("t", new BsonInt32(o.get("t").getAsInt()))
                    .append("da", new BsonInt32(o.get("da").getAsInt())).append("du", new BsonInt32(o.get("du").getAsShort()))
                    .append("ta", new BsonString(o.get("ta").getAsString()));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return doc;
    }
}
