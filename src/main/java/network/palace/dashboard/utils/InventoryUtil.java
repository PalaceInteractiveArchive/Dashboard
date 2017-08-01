package network.palace.dashboard.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.InventoryCache;
import network.palace.dashboard.handlers.InventoryUpdate;
import network.palace.dashboard.handlers.ResortInventory;
import network.palace.dashboard.packets.inventory.PacketInventoryContent;
import network.palace.dashboard.packets.inventory.Resort;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                        if (!inv.getSqlBackpackHash().equals(inv.getBackpackHash())) {
                            update.setValue(inv.getResort(), "backpack", inv.getBackpackJSON());
                            inv.setSqlBackpackHash(inv.getBackpackHash());
                        }
                        if (!inv.getSqlLockerHash().equals(inv.getLockerHash())) {
                            update.setValue(inv.getResort(), "locker", inv.getLockerJSON());
                            inv.setSqlLockerHash(inv.getLockerHash());
                        }
                        if (!inv.getSqlHotbarHash().equals(inv.getHotbarHash())) {
                            update.setValue(inv.getResort(), "hotbar", inv.getHotbarJSON());
                            inv.setSqlHotbarHash(inv.getHotbarHash());
                        }
                    }
                    if (update.shouldUpdate()) {
                        dashboard.getSchedulerManager().runAsync(new Runnable() {
                            @Override
                            public void run() {
                                updateData(cache.getUuid(), update);
                            }
                        });
                    }
                    if (dashboard.getPlayer(cache.getUuid()) == null) {
                        cachedInventories.remove(cache.getUuid());
                    }
                }
            }
        }, 1000, 300000);
    }

    /**
     * Cache a player's inventory
     *
     * @param uuid      the uuid of the player to cache
     * @param inventory the player's inventory
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
        Optional<Connection> connection = Launcher.getDashboard().getSqlUtil().getConnection();
        if (!connection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return new ResortInventory();
        }
        try {
            PreparedStatement sql = connection.get().prepareStatement("INSERT INTO storage2 (uuid, pack, packsize, " +
                    "locker, lockersize, hotbar, resort) VALUES (?,?,0,?,0,?,?)");
            sql.setString(1, uuid.toString());
            sql.setString(2, "{}");
            sql.setString(3, "{}");
            sql.setString(4, "{}");
            sql.setInt(5, resort.getId());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            ErrorUtil.logError("Error in InventoryUtil method createResortInventory", e);
        }
        return new ResortInventory();
    }

    /**
     * Get all inventory values from database for a specific user
     *
     * @param uuid the player's uuid
     * @return A cache with all of the player's resort inventories
     */
    private InventoryCache getInventoryFromDatabase(UUID uuid) {
        HashMap<Resort, ResortInventory> map = new HashMap<>();
        List<Integer> deleteRowIds = new ArrayList<>();
        Dashboard dashboard = Launcher.getDashboard();
        Optional<Connection> connection = dashboard.getSqlUtil().getConnection();
        if (!connection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return new InventoryCache(uuid, map);
        }
        try {
            PreparedStatement sql = connection.get().prepareStatement("SELECT id,pack,packsize,locker,lockersize,hotbar,resort FROM storage2 WHERE uuid=?");
            sql.setString(1, uuid.toString());
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                int id = result.getInt("id");
                String backpack = result.getString("pack");
                String locker = result.getString("locker");
                String hotbar = result.getString("hotbar");
                Resort resort = Resort.fromId(result.getInt("resort"));
                if (map.get(resort) != null) {
                    deleteRowIds.add(id);
                    continue;
                }
                ResortInventory inv = new ResortInventory(resort, backpack, generateHash(backpack), "",
                        result.getInt("packsize"), locker, generateHash(locker), "",
                        result.getInt("lockersize"), hotbar, generateHash(hotbar), "");
                map.put(resort, inv);
            }
            result.close();
            sql.close();
            if (!deleteRowIds.isEmpty()) {
                StringBuilder q = new StringBuilder("DELETE FROM storage2 WHERE ");
                for (int i = 0; i < deleteRowIds.size(); i++) {
                    q.append("id=?");
                    if (i < (deleteRowIds.size() - 1)) {
                        q.append(" OR ");
                    }
                }
                PreparedStatement delete = connection.get().prepareStatement(q.toString());
                int slot = 1;
                for (Integer deleteRowId : deleteRowIds) {
                    delete.setInt(slot, deleteRowId);
                    slot++;
                }
                System.out.println("DELETE " + q);
                delete.execute();
                delete.close();
            }
        } catch (SQLException e) {
            ErrorUtil.logError("Error in InventoryUtil method getInventoryFromDatabase", e);
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
        Optional<Connection> connection = dashboard.getSqlUtil().getConnection();
        if (!connection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return new ResortInventory();
        }
        ResortInventory inv = null;
        try {
            PreparedStatement sql = connection.get().prepareStatement("SELECT id,pack,packsize,locker,lockersize,hotbar,resort FROM storage2 WHERE uuid=? AND resort=?");
            sql.setString(1, uuid.toString());
            sql.setInt(2, resort.getId());
            ResultSet result = sql.executeQuery();
            if (!result.next()) {
                result.close();
                sql.close();
                return new ResortInventory();
            }
            int id = result.getInt("id");
            String backpack = result.getString("pack");
            String locker = result.getString("locker");
            String hotbar = result.getString("hotbar");
            inv = new ResortInventory(resort, backpack, generateHash(backpack), "",
                    result.getInt("packsize"), locker, generateHash(locker), "",
                    result.getInt("lockersize"), hotbar, generateHash(hotbar), "");
            result.close();
            sql.close();
        } catch (SQLException e) {
            ErrorUtil.logError("Error in InventoryUtil method getResortInventoryFromDatabase", e);
        }
        return inv;
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
        HashMap<Resort, HashMap<String, String>> map = update.getMap();
        for (Map.Entry<Resort, HashMap<String, String>> entry : map.entrySet()) {
            HashMap<String, String> valueMap = entry.getValue();
            StringBuilder values = new StringBuilder();
            List<String> keys = new ArrayList<>(valueMap.keySet());
            for (int i = 0; i < valueMap.size(); i++) {
                values.append(keys.get(i).replace("backpack", "pack")).append("=?");
                if (i < (valueMap.size() - 1)) {
                    values.append(", ");
                }
            }
            Optional<Connection> connection = Launcher.getDashboard().getSqlUtil().getConnection();
            if (!connection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try {
                PreparedStatement sql = connection.get().prepareStatement("UPDATE storage2 SET " + values + " WHERE uuid=? AND resort=?");
                int i = 1;
                for (String s : valueMap.values()) {
                    sql.setString(i, s);
                    i++;
                }
                sql.setString(i, uuid.toString());
                sql.setInt(i += 1, entry.getKey().getId());
                sql.execute();
                sql.close();
            } catch (SQLException e) {
                ErrorUtil.logError("Error in InventoryUtil method updateData", e);
            }
        }
    }
}
