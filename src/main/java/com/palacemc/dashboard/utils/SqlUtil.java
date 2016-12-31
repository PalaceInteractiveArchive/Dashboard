package com.palacemc.dashboard.utils;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;
import com.palacemc.dashboard.packets.dashboard.PacketPlayerRank;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marc on 7/14/16
 */
public class SqlUtil {
    private BoneCP connectionPool = null;
    public String myMCMagicConnString;

    private Dashboard dashboard = Launcher.getDashboard();

    public SqlUtil() {
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        BoneCPConfig config = new BoneCPConfig();
        String address = "";
        String database = "";
        String username = "";
        String password = "";

        try (BufferedReader br = new BufferedReader(new FileReader("sql.txt"))) {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        config.setJdbcUrl("jdbc:mysql://" + address + ":3306/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMinConnectionsPerPartition(30);
        config.setMaxConnectionsPerPartition(300);
        config.setPartitionCount(3);
        config.setIdleConnectionTestPeriod(600, TimeUnit.SECONDS);

        try {
            connectionPool = new BoneCP(config);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        BoneCPConfig mymcm = new BoneCPConfig();
        mymcm.setJdbcUrl("jdbc:mysql://" + address + ":3306/mymcmagic");
        mymcm.setUsername(username);
        mymcm.setPassword(password);
        mymcm.setMinConnectionsPerPartition(30);
        mymcm.setMaxConnectionsPerPartition(300);
        mymcm.setPartitionCount(2);
        mymcm.setIdleConnectionTestPeriod(600, TimeUnit.SECONDS);

        try {
            dashboard.setActivityUtil(new ActivityUtil(new BoneCP(mymcm)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public void stop() {
        connectionPool.shutdown();
        dashboard.getActivityUtil().stop();
    }

    /**
     * Player Methods
     */

    public void login(final Player player) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("SELECT rank,ipAddress,username,toggled,mentions,onlinetime,tutorial FROM player_data WHERE uuid=?");
                sql.setString(1, player.getUuid().toString());
                ResultSet result = sql.executeQuery();

                if (!result.next()) {
                    newPlayer(player, connection);
                    result.close();
                    sql.close();
                    return;
                }

                Rank rank = Rank.fromString(result.getString("rank"));
                if (rank.getRankId() != Rank.SETTLER.getRankId()) {
                    PacketPlayerRank packet = new PacketPlayerRank(player.getUuid(), rank);
                    player.send(packet);
                }

                boolean needsUpdate = false;
                if (!player.getAddress().equals(result.getString("ipAddress")) || !player.getUsername().equals(result.getString("username"))) {
                    needsUpdate = true;
                }

                player.setRank(rank);
                player.setToggled(result.getInt("toggled") == 1);
                player.setMentions(result.getInt("mentions") == 1);
                player.setOnlineTime(result.getLong("onlinetime"));
                player.setNewGuest(result.getInt("tutorial") != 1);

                dashboard.addPlayer(player);
                dashboard.addToCache(player.getUuid(), player.getUsername());

                result.close();
                sql.close();

                if (needsUpdate) {
                    update(player, connection);
                }

                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                            rank.getNameWithBrackets() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked in.";
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                            tp.sendMessage(msg);
                        }
                    }
                    staffClock(player.getUuid(), true, connection);
                    if (rank.getRankId() >= Rank.SQUIRE.getRankId() && dashboard.getChatUtil().isChatMuted("ParkChat")) {
                        player.sendMessage(ChatColor.RED + "\n\n\nChat is currently muted!\n\n\n");
                    }
                }

                HashMap<UUID, String> friends = getFriendList(player.getUuid());
                HashMap<UUID, String> requests = getRequestList(player.getUuid());

                player.setFriends(friends);
                player.setRequests(requests);

                HashMap<UUID, String> friendList = player.getFriends();
                if (!friendList.isEmpty()) {
                    String joinMessage = rank.getTagColor() + player.getUsername() + ChatColor.LIGHT_PURPLE + " has joined.";
                    if (rank.getRankId() >= Rank.SQUIRE.getRankId()) {
                        for (Map.Entry<UUID, String> entry : friendList.entrySet()) {
                            Player tp = dashboard.getPlayer(entry.getKey());
                            if (tp == null) return;

                            if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
                                tp.sendMessage(joinMessage);
                            }
                        }
                    } else {
                        for (Map.Entry<UUID, String> friend : friendList.entrySet()) {
                            Player tp = dashboard.getPlayer(friend.getKey());
                            if (tp != null) {
                                tp.sendMessage(joinMessage);
                            }
                        }
                    }
                }

                Mute mute = getMute(player.getUuid(), player.getUsername());
                player.setMute(mute);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void newPlayer(Player player, Connection connection) throws SQLException {
        player.setNewGuest(true);
        PreparedStatement statement = connection.prepareStatement("INSERT INTO player_data (uuid, username, ipAddress) VALUES(?,?,?)");

        statement.setString(1, player.getUuid().toString());
        statement.setString(2, player.getUsername());
        statement.setString(3, player.getAddress());

        statement.execute();
        statement.close();

        dashboard.addPlayer(player);
    }

    private void update(Player player, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET username=?,ipAddress=? WHERE uuid=?");

        statement.setString(1, player.getUsername());
        statement.setString(2, player.getAddress());
        statement.setString(3, player.getUuid().toString());

        statement.execute();
        statement.close();
    }

    public void silentJoin(final Player player) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT toggled,mentions,onlinetime FROM player_data WHERE uuid=?");

            statement.setString(1, player.getUuid().toString());
            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                return;
            }

            Rank rank = player.getRank();

            player.setToggled(result.getInt("toggled") == 1);
            player.setMentions(result.getInt("mentions") == 1);
            player.setOnlineTime(result.getLong("onlinetime"));

            dashboard.addPlayer(player);
            dashboard.addToCache(player.getUuid(), player.getUsername());

            result.close();
            statement.close();

            HashMap<UUID, String> friends = getFriendList(player.getUuid());
            HashMap<UUID, String> requests = getRequestList(player.getUuid());

            player.setFriends(friends);
            player.setRequests(requests);

            Mute mute = getMute(player.getUuid(), player.getUsername());
            player.setMute(mute);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logout(final Player player) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET server=?,lastseen=?,onlinetime = onlinetime+? WHERE uuid=?");

                if (player.getServer() != null) {
                    statement.setString(1, player.getServer());
                } else {
                    statement.setString(1, "Unknown");
                }

                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                statement.setInt(3, (int) ((System.currentTimeMillis() / 1000) - (player.getLoginTime() / 1000)));
                statement.setString(4, player.getUuid().toString());
                statement.execute();
                statement.close();

                Rank rank = player.getRank();
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                            rank.getNameWithBrackets() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked out.";
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                            tp.sendMessage(msg);
                        }
                    }
                    staffClock(player.getUuid(), false, connection);
                }

                HashMap<UUID, String> friendList = player.getFriends();
                if (!friendList.isEmpty()) {
                    String joinMessage = rank.getTagColor() + player.getUsername() + ChatColor.LIGHT_PURPLE + " has left.";
                    if (rank.getRankId() >= Rank.SQUIRE.getRankId()) {
                        for (Map.Entry<UUID, String> entry : friendList.entrySet()) {
                            Player tp = dashboard.getPlayer(entry.getKey());
                            if (tp != null) {
                                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
                                    tp.sendMessage(joinMessage);
                                }
                            }
                        }
                    } else {
                        for (Map.Entry<UUID, String> entry : friendList.entrySet()) {
                            Player tp = dashboard.getPlayer(entry.getKey());
                            if (tp != null) {
                                tp.sendMessage(joinMessage);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public UUID uuidFromUsername(String username) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT uuid FROM player_data WHERE username=?");
            sql.setString(1, username);
            ResultSet result = sql.executeQuery();
            if (!result.next()) {
                result.close();
                sql.close();
                return null;
            }
            String uuid = result.getString("uuid");
            sql.close();
            result.close();
            return UUID.fromString(uuid);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String usernameFromUUID(UUID uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT username FROM player_data WHERE uuid=?");
            sql.setString(1, uuid.toString());
            ResultSet result = sql.executeQuery();
            if (!result.next()) {
                result.close();
                sql.close();
                return "unknown";
            }
            String username = result.getString("username");
            sql.close();
            result.close();
            return username;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<UUID, String> getFriendList(UUID uuid) {
        return getList(uuid, 1);
    }

    public HashMap<UUID, String> getRequestList(UUID uuid) {
        return getList(uuid, 0);
    }

    private HashMap<UUID, String> getList(UUID uuid, int status) {
        List<UUID> uuids = new ArrayList<>();
        HashMap<UUID, String> map = new HashMap<>();

        try (Connection connection = getConnection()) {
            switch (status) {
                case 0:
                    PreparedStatement statement = connection.prepareStatement("SELECT sender FROM friends WHERE receiver=? AND status=0");
                    statement.setString(1, uuid.toString());
                    ResultSet result = statement.executeQuery();

                    while (result.next()) {
                        UUID senderUUID = UUID.fromString(result.getString("sender"));
                        String name = dashboard.getCachedName(senderUUID);
                        if (name == null) {
                            uuids.add(senderUUID);
                        } else {
                            map.put(senderUUID, name);
                        }
                    }
                    result.close();
                    statement.close();
                    break;
                case 1:
                    statement = connection.prepareStatement("SELECT sender,receiver FROM friends WHERE (sender=? OR receiver=?) AND status=1");

                    statement.setString(1, uuid.toString());
                    statement.setString(2, uuid.toString());

                    result = statement.executeQuery();

                    while (result.next()) {
                        UUID senderUUID;
                        if (result.getString("sender").equalsIgnoreCase(uuid.toString())) {
                            senderUUID = UUID.fromString(result.getString("receiver"));
                        } else {
                            senderUUID = UUID.fromString(result.getString("sender"));
                        }

                        String senderName = dashboard.getCachedName(senderUUID);
                        if (senderName == null) {
                            uuids.add(senderUUID);
                        } else {
                            map.put(senderUUID, senderName);
                        }
                    }
                    break;
            }

            if (uuids.isEmpty()) {
                return map;
            }

            String query = "SELECT username,uuid FROM player_data WHERE uuid=";
            for (int i = 0; i < uuids.size(); i++) {
                if (i >= (uuids.size() - 1)) {
                    query += "?";
                } else {
                    query += "? or uuid=";
                }
            }

            PreparedStatement sql2 = connection.prepareStatement(query);
            for (int i = 1; i < (uuids.size() + 1); i++) {
                sql2.setString(i, uuids.get(i - 1).toString());
            }

            ResultSet res2 = sql2.executeQuery();
            while (res2.next()) {
                UUID tuuid = UUID.fromString(res2.getString("uuid"));
                String name = res2.getString("username");
                map.put(tuuid, name);
                dashboard.addToCache(tuuid, name);
            }

            res2.close();
            sql2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<String> getNamesFromIP(String address) {
        List<String> names = new ArrayList<>();
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT username FROM player_data WHERE ipAddress=?");

            statement.setString(1, address);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                names.add(result.getString("username"));
            }

            result.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }

    public HashMap<Rank, List<UUID>> getPlayersByRanks(Rank... ranks) {
        HashMap<Rank, List<UUID>> playerRanks = new HashMap<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT uuid,rank FROM player_data WHERE rank=?";

            for (int i = 1; i < ranks.length; i++) {
                query += " OR rank=?";
            }

            PreparedStatement statement = connection.prepareStatement(query);
            for (int i = 1; i <= ranks.length; i++) {
                statement.setString(i, ranks[i - 1].getSqlName());
            }

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("uuid"));
                Rank rank = Rank.fromString(result.getString("rank"));

                if (!playerRanks.containsKey(rank)) {
                    List<UUID> list = new ArrayList<>();
                    list.add(uuid);
                    playerRanks.put(rank, list);
                } else {
                    playerRanks.get(rank).add(uuid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerRanks;
    }

    private void staffClock(UUID uuid, boolean in, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO staffclock (id, user, action, time) " +
                "VALUES(0, ?, ?, ?)");

        statement.setString(1, uuid.toString());
        statement.setString(2, in ? "login" : "logout");
        statement.setLong(3, System.currentTimeMillis() / 1000);

        statement.execute();
        statement.close();
    }

    public void completeTutorial(final UUID uuid) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET tutorial=1 WHERE uuid=?");

                statement.setString(1, uuid.toString());

                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Ban Methods
     */
    public void banPlayer(final UUID uuid, final String reason, final boolean permanent, final Date date, final String source) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO banned_players (uuid,reason,permanent,`release`,source) VALUES (?,?,?,?,?)");

                statement.setString(1, uuid.toString());
                statement.setString(2, reason);
                statement.setInt(3, permanent ? 1 : 0);
                statement.setTimestamp(4, new Timestamp(date.getTime()));
                statement.setString(5, source);

                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isBannedPlayer(UUID uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT active FROM banned_players WHERE uuid=?");

            statement.setString(1, uuid.toString());

            ResultSet results = statement.executeQuery();

            boolean banned = false;
            while (results.next()) {
                if (results.getInt("active") == 1) {
                    banned = true;
                    break;
                }
            }

            statement.close();
            results.close();

            return banned;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void banIP(final String ip, final String reason, final String source) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO banned_ips values(0,?,?,?,1)");

                statement.setString(1, ip);
                statement.setString(2, reason);
                statement.setString(3, source);

                statement.execute();
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Ban getBan(UUID uuid, String name) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT permanent,`release`,reason,source FROM banned_players WHERE uuid=? AND active=1;");

            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                return null;
            }

            Ban ban = new Ban(uuid, name, result.getInt("permanent") == 1, result.getTimestamp("release").getTime(),
                    result.getString("reason"), result.getString("source"));

            result.close();
            statement.close();
            return ban;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AddressBan getAddressBan(String address) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT ipAddress,reason,source FROM banned_ips WHERE ipAddress=? AND active=1;");

            statement.setString(1, address);
            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                return null;
            }

            AddressBan ban = new AddressBan(result.getString("ipAddress"), result.getString("reason"), result.getString("source"));
            result.close();
            statement.close();
            return ban;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unbanPlayer(final UUID uuid) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE banned_players SET active=0 WHERE uuid=?");

                statement.setString(1, uuid.toString());

                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void unbanIP(final String address) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE banned_ips SET active=0 WHERE ipAddress=?");

                statement.setString(1, address);

                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Mute Methods
     */
    public Mute getMute(UUID uuid, String username) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT reason,`release`,source,active FROM muted_players WHERE uuid=?");

            sql.setString(1, uuid.toString());

            ResultSet result = sql.executeQuery();
            Mute mute = null;

            while (result.next()) {
                if (result.getInt("active") == 1) {
                    mute = new Mute(uuid, result.getString("reason"), result.getString("reason"),
                            result.getString("source"), true, result.getInt("release"));
                }
            }

            if (mute == null) {
                result.close();
                sql.close();
                return new Mute(uuid, username, "", "", false, System.currentTimeMillis());
            }

            result.close();
            sql.close();
            return mute;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void mutePlayer(Mute mute) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO muted_players (uuid,`release`,source,reason) VALUES (?,?,?,?)");

            statement.setString(1, mute.getUuid().toString());
            statement.setTimestamp(2, new Timestamp(mute.getRelease()));
            statement.setString(3, mute.getSource());
            statement.setString(4, mute.getReason());

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isMuted(UUID uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT active FROM muted_players WHERE uuid=? AND active=1");

            statement.setString(1, uuid.toString());
            boolean muted = false;

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                if (result.getInt("active") == 1) {
                    muted = true;
                    break;
                }
            }

            result.close();
            statement.close();
            return muted;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unmutePlayer(final UUID uuid) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE muted_players SET active=0 WHERE uuid=?");
                statement.setString(1, uuid.toString());

                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Kick Methods
     */

    public void logKick(final Kick kick) {
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO kicks (uuid, reason, source) VALUES (?,?,?)");

                statement.setString(1, kick.getUuid().toString());
                statement.setString(2, kick.getReason());
                statement.setString(3, kick.getSource());

                statement.execute();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}