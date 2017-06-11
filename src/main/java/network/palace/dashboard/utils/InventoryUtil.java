package network.palace.dashboard.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.InventoryCache;
import network.palace.dashboard.handlers.InventoryUpdate;
import network.palace.dashboard.handlers.Player;
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
                        String packJSON = ob.get("packJSON").getAsString();
                        String packHash = ob.get("packHash").getAsString();
                        String sqlPackHash = ob.get("sqlPackHash").getAsString();
                        int packsize = ob.get("packsize").getAsInt();
                        String lockerJSON = ob.get("lockerJSON").getAsString();
                        String lockerHash = ob.get("lockerHash").getAsString();
                        String sqlLockerHash = ob.get("sqlLockerHash").getAsString();
                        int lockersize = ob.get("lockersize").getAsInt();
                        String hotbarJSON = ob.get("hotbarJSON").getAsString();
                        String hotbarHash = ob.get("hotbarHash").getAsString();
                        String sqlHotbarHash = ob.get("sqlHotbarHash").getAsString();
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
                for (Player p : Launcher.getDashboard().getOnlinePlayers()) {
                    InventoryCache cache = cachedInventories.get(p.getUniqueId());
                    if (cache == null || cache.getResorts() == null) {
                        continue;
                    }
                    InventoryUpdate update = new InventoryUpdate();
                    for (ResortInventory inv : cache.getResorts().values()) {
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
                                updateData(p.getUniqueId(), update);
                            }
                        });
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
            cachedInventories.get(uuid).setInventory(packet.getResort(), new ResortInventory(packet.getResort(),
                    packet.getBackpackJson(), packet.getBackpackHash(), "", packet.getBackpackSize(),
                    packet.getLockerJson(), packet.getLockerHash(), "", packet.getLockerSize(),
                    packet.getHotbarJson(), packet.getHotbarHash(), ""));
            return;
        }
        HashMap<Resort, ResortInventory> map = new HashMap<>();
        map.put(packet.getResort(), new ResortInventory(packet.getResort(), packet.getBackpackJson(), packet.getBackpackHash(),
                "", packet.getBackpackSize(), packet.getLockerJson(), packet.getLockerHash(),
                "", packet.getLockerSize(), packet.getHotbarJson(), packet.getHotbarHash(), ""));
        InventoryCache cache = new InventoryCache(uuid, map);
        cachedInventories.put(uuid, cache);
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
            return new ResortInventory();
        }
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
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT pack,packsize,locker,lockersize,hotbar,resort FROM storage2 WHERE uuid=?");
            sql.setString(1, uuid.toString());
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                String backpack = result.getString("pack");
                String locker = result.getString("locker");
                String hotbar = result.getString("hotbar");
                Resort resort = Resort.fromId(result.getInt("resort"));
                ResortInventory inv = new ResortInventory(resort, backpack, generateHash(backpack), "",
                        result.getInt("packsize"), locker, generateHash(locker), "",
                        result.getInt("lockersize"), hotbar, generateHash(hotbar), "");
                map.put(resort, inv);
            }
            result.close();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new InventoryCache(uuid, map);
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
            try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE storage2 SET " + values + " WHERE uuid=? AND resort=?");
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
                e.printStackTrace();
            }
        }
    }
}
