package network.palace.dashboard.forums;

import network.palace.dashboard.forums.db.Database;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketLink;

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
            String key = addNewKey(player.getUniqueId().toString(), player.getUsername(), player.getRank().getName());
            String link = "https://palace.network/link-minecraft/?key=" + key + "&type=link";
            PacketLink packet = new PacketLink(player.getUniqueId(), link, "Click to link your Minecraft and Palace Forum accounts", ChatColor.YELLOW, true, true);
            player.send(packet);
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error connecting your Forum account, try again soon!");
        }
    }

    public String addNewKey(String uuid, String username, String rank) throws SQLException {
        String doesKeyExist = this._doesKeyExist(uuid);
        return !doesKeyExist.equalsIgnoreCase("false") ? doesKeyExist : this._addNewKey(uuid, username, rank);
    }

    private String _doesKeyExist(final String uuid) {
        boolean keyExists = false;
        String existing = "";
        try {
            String e = (String) Database.getFirstColumn("SELECT token FROM apms2_key WHERE uuid = ? AND valid = 1 AND key_type = 1 LIMIT 1", uuid);
            if (e != null) {
                keyExists = true;
                existing = e;
            }
        } catch (SQLException var5) {
            var5.printStackTrace();
        }
        return !keyExists ? String.valueOf(false) : existing;
    }

    private String _addNewKey(final String uuid, String username, String rank) throws SQLException {
        String token = UUID.randomUUID().toString();
        long unixTime = System.currentTimeMillis() / 1000L;
        Database.executeUpdate("INSERT INTO apms2_key (token, uuid, mc_username, rank, valid, create_date, key_type) VALUES (?, ?, ?, ?, ?, ?, ?)", token, uuid, username, rank, 1, Long.toString(unixTime), 1);
        return token;
    }

    public void updatePlayerName(String uuid, String username) throws SQLException {
        Database.executeUpdate("UPDATE xf_user SET apms2_username=? WHERE apms2_uuid=?", username, uuid);
    }

    public void updatePlayerRank(String uuid, String rank) throws SQLException {
        int rankid = 0;
        switch (rank) {
            case "settler":
                rankid = 1;
                break;
            case "dweller":
                rankid = 2;
                break;
            case "noble":
                rankid = 3;
                break;
            case "majestic":
                rankid = 4;
                break;
            case "honorable":
                rankid = 5;
                break;
            case "specialguest":
                rankid = 6;
                break;
            case "mcprohosting":
                rankid = 7;
                break;
            case "squire":
                rankid = 8;
                break;
            case "knight":
                rankid = 9;
                break;
            case "architect":
                rankid = 10;
                break;
            case "paladin":
                rankid = 11;
                break;
            case "wizard":
                rankid = 12;
                break;
            case "emperor":
                rankid = 13;
                break;
            case "empress":
                rankid = 14;
                break;
        }
        Database.executeUpdate("UPDATE xf_user SET apms2_group=?,apms2_groups=? WHERE apms2_uuid=?", rankid, rankid, uuid);
    }
}
