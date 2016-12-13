package com.palacemc.dashboard.forums;

import com.palacemc.dashboard.forums.db.Database;
import com.palacemc.dashboard.handlers.ChatColor;
import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.packets.dashboard.PacketLink;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Marc on 12/12/16.
 */
public class Forum {
    private Database db;

    public Forum() throws IOException {
        initialize();
    }

    public void initialize() throws IOException {
        db = new Database();
        String address = "";
        String database = "";
        String username = "";
        String password = "";
        try (BufferedReader br = new BufferedReader(new FileReader("websql.txt"))) {
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
        db.initialize(address, 3306, database, username, password);
    }

    public void stop() {
        db.stop();
    }

    public void linkAccount(Player player) {
        try {
            String key = addNewKey(player.getUniqueId().toString(), player.getName());
            String link = "https://palace.network/link-minecraft/?key=" + key + "&type=link";
            PacketLink packet = new PacketLink(player.getUniqueId(), link, "Click to link your Minecraft and Palace Forum accounts", ChatColor.YELLOW, true, true);
            player.send(packet);
            _updatePlayer(player.getUniqueId().toString(), player.getName(), player.getRank().getSqlName());
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error connecting your Forum account, try again soon!");
        }
    }

    public String addNewKey(String uuid, String username) throws SQLException {
        String doesKeyExist = this._doesKeyExist(uuid);
        System.out.println(doesKeyExist);
        return !doesKeyExist.equalsIgnoreCase("false") ? doesKeyExist : this._addNewKey(uuid, username);
    }

    private String _doesKeyExist(final String uuid) {
        boolean keyExists = false;
        String existing = "";
        try {
            String e = (String) Database.getFirstColumn("SELECT token FROM apms2_key WHERE uuid = ? AND valid = 1 AND key_type = 1 LIMIT 1", uuid);
            System.out.println("E");
            if (e != null) {
                keyExists = true;
                existing = e;
                System.out.println("F");
            }
        } catch (SQLException var5) {
            var5.printStackTrace();
        }
        System.out.println("G");
        return !keyExists ? String.valueOf(false) : existing;
    }

    private String _addNewKey(final String uuid, String username) throws SQLException {
        String token = UUID.randomUUID().toString();
        System.out.println("B");
        long unixTime = System.currentTimeMillis() / 1000L;
        System.out.println("C - " + token);
        Database.executeUpdate("INSERT INTO apms2_key (token, uuid, mc_username, valid, create_date, key_type) VALUES (?, ?, ?, ?, ?, ?)", token, uuid, username, 1, Long.toString(unixTime), 1);
        System.out.println("D");
        return token;
    }

    public void _updatePlayer(String uuid, String username, String jsonGroups) throws SQLException {
        System.out.println("A");
        Database.executeUpdate("INSERT INTO apms2_updateplayercache (uuid, mc_username, groups) VALUES (?, ?, ?)", uuid, username, jsonGroups);
    }
}
