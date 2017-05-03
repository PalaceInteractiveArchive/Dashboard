package network.palace.dashboard.utils;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.discordSocket.DiscordCacheInfo;
import network.palace.dashboard.discordSocket.DiscordDatabaseInfo;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketPlayerRank;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;

import java.io.BufferedReader;
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
    BoneCP connectionPool = null;
    public String myMCMagicConnString;

    public SqlUtil() throws SQLException, IOException {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
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
        }
        config.setJdbcUrl("jdbc:mysql://" + address + ":3306/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMinConnectionsPerPartition(30);
        config.setMaxConnectionsPerPartition(300);
        config.setPartitionCount(3);
        config.setIdleConnectionTestPeriod(600, TimeUnit.SECONDS);
        connectionPool = new BoneCP(config);
        BoneCPConfig mymcm = new BoneCPConfig();
        mymcm.setJdbcUrl("jdbc:mysql://" + address + ":3306/mymcmagic");
        mymcm.setUsername(username);
        mymcm.setPassword(password);
        mymcm.setMinConnectionsPerPartition(30);
        mymcm.setMaxConnectionsPerPartition(300);
        mymcm.setPartitionCount(2);
        mymcm.setIdleConnectionTestPeriod(600, TimeUnit.SECONDS);
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.setActivityUtil(new ActivityUtil(new BoneCP(mymcm)));
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public void stop() {
        connectionPool.shutdown();
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getActivityUtil().stop();
    }

    /**
     * Player Methods
     */

    public void login(final Player player) {
        Dashboard dashboard = Launcher.getDashboard();

        dashboard.getSchedulerManager().runAsync(new Runnable() {
            @Override
            public void run() {
                // Check if the uuid is from MCLeaks before we continue.
                boolean isMCLeaks = MCLeakUtil.checkPlayer(player);
                if (isMCLeaks) {
                    // UUID is in MCLeaks, temp ban the account
                    long timestamp = DateUtil.parseDateDiff("4d", true);
                    banPlayer(player.getUuid(), "MCLeaks Account", false, new Date(timestamp), "Dashboard");
                    dashboard.getModerationUtil().announceBan(new Ban(player.getUniqueId(), player.getUsername(),
                            true, System.currentTimeMillis(), "MCLeaks Account", "Dashboard"));
                    player.kickPlayer(ChatColor.RED + "MCLeaks Accounts are not allowed on the Palace Network\n" +
                            ChatColor.AQUA + "If you think were banned incorrectly, submit an appeal at palnet.us/appeal");
                }
            }
        });

        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("SELECT rank,ipAddress,username,toggled,mentions,onlinetime,tutorial,mcversion FROM player_data WHERE uuid=?");
                sql.setString(1, player.getUniqueId().toString());
                ResultSet result = sql.executeQuery();
                if (!result.next()) {
                    newPlayer(player, connection);
                    result.close();
                    sql.close();
                    return;
                }
                Rank rank = Rank.fromString(result.getString("rank"));
                if (rank.getRankId() != Rank.SETTLER.getRankId()) {
                    PacketPlayerRank packet = new PacketPlayerRank(player.getUniqueId(), rank);
                    player.send(packet);
                }
                boolean needsUpdate = false;
                boolean isSameIP = !player.getAddress().equals(result.getString("ipAddress"));
                boolean disable = isSameIP && rank.getRankId() >= Rank.SQUIRE.getRankId();

                if (isSameIP || !player.getUsername().equals(result.getString("username")) ||
                        player.getMcVersion() != result.getInt("mcversion")) needsUpdate = true;

                player.setDisabled(disable);
                player.setRank(rank);
                player.setToggled(result.getInt("toggled") == 1);
                player.setMentions(result.getInt("mentions") == 1);
                player.setOnlineTime(result.getLong("onlinetime"));
                player.setNewGuest(result.getInt("tutorial") != 1);
                dashboard.addPlayer(player);
                dashboard.addToCache(player.getUniqueId(), player.getUsername());

                String username = result.getString("username");
                int protocolVersion = result.getInt("mcversion");
                result.close();
                sql.close();
                if (needsUpdate)
                    update(player, connection, !player.getUsername().equals(username), player.getMcVersion() != protocolVersion);
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                            rank.getNameWithBrackets() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked in.";
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                            tp.sendMessage(msg);
                        }
                    }
                    staffClock(player.getUniqueId(), true, connection);
                    if (rank.getRankId() >= Rank.SQUIRE.getRankId() && dashboard.getChatUtil().isChatMuted("ParkChat")) {
                        player.sendMessage(ChatColor.RED + "\n\n\nChat is currently muted!\n\n\n");
                    }
                }
                HashMap<UUID, String> friends = getFriendList(player.getUniqueId());
                HashMap<UUID, String> requests = getRequestList(player.getUniqueId());
                player.setFriends(friends);
                player.setRequests(requests);
                HashMap<UUID, String> friendList = player.getFriends();
                if (friendList == null) return;
                if (!friendList.isEmpty()) {
                    String joinMessage = rank.getTagColor() + player.getUsername() + ChatColor.LIGHT_PURPLE + " has joined.";
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
                Mute mute = getMute(player.getUniqueId(), player.getUsername());
                player.setMute(mute);
                if (disable) {
                    SlackMessage m = new SlackMessage("");
                    SlackAttachment a = new SlackAttachment(rank.getName() + " " + player.getUsername() +
                            " connected from a new IP address " + player.getAddress());
                    a.color("warning");
                    dashboard.getSlackUtil().sendDashboardMessage(m, Arrays.asList(a), false);
                    player.sendMessage(ChatColor.YELLOW + "\n\n" + ChatColor.BOLD +
                            "You connected with a new IP address, type " + ChatColor.GREEN + "" + ChatColor.BOLD +
                            "/staff login [password]" + ChatColor.YELLOW + "" + ChatColor.BOLD + " to verify your account.\n");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void newPlayer(Player player, Connection connection) throws SQLException {
        Dashboard dashboard = Launcher.getDashboard();
        player.setNewGuest(true);
        PreparedStatement sql = connection.prepareStatement("INSERT INTO player_data (uuid, username, ipAddress) VALUES(?,?,?)");
        sql.setString(1, player.getUniqueId().toString());
        sql.setString(2, player.getUsername());
        sql.setString(3, player.getAddress());
        sql.execute();
        sql.close();
        dashboard.addPlayer(player);
    }

    private void update(Player player, Connection connection, boolean username, boolean mcversion) throws SQLException {
        if (player.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
            if (!username && !mcversion) {
                return;
            }
            PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET username=?,mcversion=? WHERE uuid=?");
            sql.setString(1, player.getUsername());
            sql.setInt(2, player.getMcVersion());
            sql.setString(3, player.getUniqueId().toString());
            sql.execute();
            sql.close();
        } else {
            PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET username=?,mcversion=?,ipAddress=? WHERE uuid=?");
            sql.setString(1, player.getUsername());
            sql.setInt(2, player.getMcVersion());
            sql.setString(3, player.getAddress());
            sql.setString(4, player.getUniqueId().toString());
            sql.execute();
            sql.close();
        }
        if (username) {
            Launcher.getDashboard().getForum().updatePlayerName(player.getUniqueId().toString(), player.getUsername());
        }
    }

    public void updateStaffIP(Player player) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET ipAddress=? WHERE uuid=?");
                sql.setString(1, player.getAddress());
                sql.setString(2, player.getUniqueId().toString());
                sql.execute();
                sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void silentJoin(final Player player) {
        Dashboard dashboard = Launcher.getDashboard();
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT toggled,mentions,onlinetime FROM player_data WHERE uuid=?");
            sql.setString(1, player.getUniqueId().toString());
            ResultSet result = sql.executeQuery();
            if (!result.next()) {
                return;
            }
            Rank rank = player.getRank();
            player.setToggled(result.getInt("toggled") == 1);
            player.setMentions(result.getInt("mentions") == 1);
            player.setOnlineTime(result.getLong("onlinetime"));
            dashboard.addPlayer(player);
            dashboard.addToCache(player.getUniqueId(), player.getUsername());
            result.close();
            sql.close();
            HashMap<UUID, String> friends = getFriendList(player.getUniqueId());
            HashMap<UUID, String> requests = getRequestList(player.getUniqueId());
            player.setFriends(friends);
            player.setRequests(requests);
            Mute mute = getMute(player.getUniqueId(), player.getUsername());
            player.setMute(mute);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logout(final Player player) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET server=?,lastseen=?,onlinetime = onlinetime+? WHERE uuid=?");
                if (player.getServer() != null) {
                    sql.setString(1, player.getServer());
                } else {
                    sql.setString(1, "Unknown");
                }
                sql.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                sql.setInt(3, (int) ((System.currentTimeMillis() / 1000) - (player.getLoginTime() / 1000)));
                sql.setString(4, player.getUniqueId().toString());
                sql.execute();
                sql.close();
                Rank rank = player.getRank();
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                            rank.getNameWithBrackets() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked out.";
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                            tp.sendMessage(msg);
                        }
                    }
                    staffClock(player.getUniqueId(), false, connection);
                }
                HashMap<UUID, String> flist = player.getFriends();
                if (!flist.isEmpty()) {
                    String joinMessage = rank.getTagColor() + player.getUsername() + ChatColor.LIGHT_PURPLE + " has left.";
                    if (rank.getRankId() >= Rank.SQUIRE.getRankId()) {
                        for (Map.Entry<UUID, String> entry : flist.entrySet()) {
                            Player tp = dashboard.getPlayer(entry.getKey());
                            if (tp != null) {
                                if (tp.getRank().getRankId() < Rank.SQUIRE.getRankId()) {
                                    tp.sendMessage(joinMessage);
                                }
                            }
                        }
                    } else {
                        for (Map.Entry<UUID, String> entry : flist.entrySet()) {
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
        Dashboard dashboard = Launcher.getDashboard();
        List<UUID> uuids = new ArrayList<>();
        HashMap<UUID, String> map = new HashMap<>();
        try (Connection connection = getConnection()) {
            switch (status) {
                case 0: {
                    PreparedStatement sql = connection.prepareStatement("SELECT sender FROM friends WHERE receiver=? AND status=0");
                    sql.setString(1, uuid.toString());
                    ResultSet result = sql.executeQuery();
                    while (result.next()) {
                        UUID tuuid = UUID.fromString(result.getString("sender"));
                        String name = dashboard.getCachedName(tuuid);
                        if (name == null) {
                            uuids.add(tuuid);
                        } else {
                            map.put(tuuid, name);
                        }
                    }
                    result.close();
                    sql.close();
                    break;
                }
                case 1: {
                    PreparedStatement sql = connection.prepareStatement("SELECT sender,receiver FROM friends WHERE (sender=? OR receiver=?) AND status=1");
                    sql.setString(1, uuid.toString());
                    sql.setString(2, uuid.toString());
                    ResultSet result = sql.executeQuery();
                    while (result.next()) {
                        UUID tuuid;
                        if (result.getString("sender").equalsIgnoreCase(uuid.toString())) {
                            tuuid = UUID.fromString(result.getString("receiver"));
                        } else {
                            tuuid = UUID.fromString(result.getString("sender"));
                        }
                        String name = dashboard.getCachedName(tuuid);
                        if (name == null) {
                            uuids.add(tuuid);
                        } else {
                            map.put(tuuid, name);
                        }
                    }
                    break;
                }
            }
            if (uuids.isEmpty()) {
                return map;
            }
            StringBuilder query = new StringBuilder("SELECT username,uuid FROM player_data WHERE uuid=");
            for (int i = 0; i < uuids.size(); i++) {
                if (i >= (uuids.size() - 1)) {
                    query.append("?");
                } else {
                    query.append("? or uuid=");
                }
            }
            PreparedStatement sql2 = connection.prepareStatement(query.toString());
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
        List<String> list = new ArrayList<>();
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT username FROM player_data WHERE ipAddress=?");
            sql.setString(1, address);
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                list.add(result.getString("username"));
            }
            result.close();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public HashMap<Rank, List<UUID>> getPlayersByRanks(Rank... ranks) {
        HashMap<Rank, List<UUID>> map = new HashMap<>();
        try (Connection connection = getConnection()) {
            StringBuilder q = new StringBuilder("SELECT uuid,rank FROM player_data WHERE rank=?");
            for (int i = 1; i < ranks.length; i++) {
                q.append(" OR rank=?");
            }
            PreparedStatement sql = connection.prepareStatement(q.toString());
            for (int i = 1; i <= ranks.length; i++) {
                sql.setString(i, ranks[i - 1].getSqlName());
            }
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                UUID uuid = UUID.fromString(result.getString("uuid"));
                Rank rank = Rank.fromString(result.getString("rank"));
                if (!map.containsKey(rank)) {
                    List<UUID> list = new ArrayList<>();
                    list.add(uuid);
                    map.put(rank, list);
                } else {
                    map.get(rank).add(uuid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void staffClock(UUID uuid, boolean in, Connection connection) throws SQLException {
        PreparedStatement sql = connection.prepareStatement("INSERT INTO staffclock (id, user, action, time) " +
                "VALUES(0, ?, ?, ?)");
        sql.setString(1, uuid.toString());
        sql.setString(2, in ? "login" : "logout");
        sql.setLong(3, System.currentTimeMillis() / 1000);
        sql.execute();
        sql.close();
    }

    public void completeTutorial(final UUID uuid) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET tutorial=1 WHERE uuid=?");
                sql.setString(1, uuid.toString());
                sql.execute();
                sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Ban Methods
     */
    public void banPlayer(final Ban ban) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("INSERT INTO banned_players (uuid,reason,permanent,`release`,source) VALUES (?,?,?,?,?)");
                sql.setString(1, ban.getUniqueId().toString());
                sql.setString(2, ban.getReason());
                sql.setInt(3, ban.isPermanent() ? 1 : 0);
                sql.setTimestamp(4, new Timestamp(new Date(ban.getRelease()).getTime()));
                sql.setString(5, ban.getSource());
                sql.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void banPlayer(final UUID uuid, final String reason, final boolean permanent, final Date date, final String source) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("INSERT INTO banned_players (uuid,reason,permanent,`release`,source) VALUES (?,?,?,?,?)");
                sql.setString(1, uuid.toString());
                sql.setString(2, reason);
                sql.setInt(3, permanent ? 1 : 0);
                sql.setTimestamp(4, new Timestamp(date.getTime()));
                sql.setString(5, source);
                sql.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isBannedPlayer(UUID uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT active FROM banned_players WHERE uuid=?");
            sql.setString(1, uuid.toString());
            ResultSet results = sql.executeQuery();
            boolean banned = false;
            while (results.next()) {
                if (results.getInt("active") == 1) {
                    banned = true;
                    break;
                }
            }
            sql.close();
            results.close();
            return banned;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void banIP(final String ip, final String reason, final String source) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("INSERT INTO banned_ips values(0,?,?,?,1)");
                sql.setString(1, ip);
                sql.setString(2, reason);
                sql.setString(3, source);
                sql.execute();
                sql.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Ban getBan(UUID uuid, String name) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT permanent,`release`,reason,source FROM banned_players WHERE uuid=? AND active=1;");
            sql.setString(1, uuid.toString());
            ResultSet result = sql.executeQuery();
            Ban ban;
            if (!result.next()) {
                return null;
            }
            ban = new Ban(uuid, name, result.getInt("permanent") == 1, result.getTimestamp("release").getTime(),
                    result.getString("reason"), result.getString("source"));
            result.close();
            sql.close();
            return ban;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AddressBan getAddressBan(String address) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT ipAddress,reason,source FROM banned_ips WHERE ipAddress=? AND active=1;");
            sql.setString(1, address);
            ResultSet result = sql.executeQuery();
            AddressBan ban;
            if (!result.next()) {
                return null;
            }
            ban = new AddressBan(result.getString("ipAddress"), result.getString("reason"), result.getString("source"));
            result.close();
            sql.close();
            return ban;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unbanPlayer(final UUID uuid) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE banned_players SET active=0 WHERE uuid=?");
                sql.setString(1, uuid.toString());
                sql.execute();
                sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void unbanIP(final String address) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE banned_ips SET active=0 WHERE ipAddress=?");
                sql.setString(1, address);
                sql.execute();
                sql.close();
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
                    mute = new Mute(uuid, username, true, result.getTimestamp("release").getTime(),
                            result.getString("reason"), result.getString("source"));
                }
            }
            if (mute == null) {
                result.close();
                sql.close();
                return new Mute(uuid, username, false, System.currentTimeMillis(), "", "");
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
            PreparedStatement sql = connection.prepareStatement("INSERT INTO muted_players (uuid,`release`,source,reason) VALUES (?,?,?,?)");
            sql.setString(1, mute.getUniqueId().toString());
            sql.setTimestamp(2, new Timestamp(mute.getRelease()));
            sql.setString(3, mute.getSource());
            sql.setString(4, mute.getReason());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isMuted(UUID uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT active FROM muted_players WHERE uuid=? AND active=1");
            sql.setString(1, uuid.toString());
            boolean muted = false;
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                if (result.getInt("active") == 1) {
                    muted = true;
                    break;
                }
            }
            result.close();
            sql.close();
            return muted;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unmutePlayer(final UUID uuid) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("UPDATE muted_players SET active=0 WHERE uuid=?");
                sql.setString(1, uuid.toString());
                sql.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Kick Methods
     */

    public void logKick(final Kick kick) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("INSERT INTO kicks (uuid, reason, source) VALUES (?,?,?)");
                sql.setString(1, kick.getUniqueId().toString());
                sql.setString(2, kick.getReason());
                sql.setString(3, kick.getSource());
                sql.execute();
                sql.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Discord Methods
     */

    public void insertDiscord(final DiscordCacheInfo cacheInfo) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            dashboard.getLogger().info("discord insert");
            try (Connection connection = getConnection()) {
                PreparedStatement sql = connection.prepareStatement("INSERT INTO discord (minecraftUsername, minecraftUUID, discordUsername) VALUES (?,?,?)");
                sql.setString(1, cacheInfo.getMinecraft().getUsername());
                sql.setString(2, cacheInfo.getMinecraft().getUuid());
                sql.setString(3, cacheInfo.getDiscord().getUsername());
                dashboard.getLogger().info("insert");
                sql.execute();
                sql.close();
                SocketConnection.sendNewlink(cacheInfo);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void selectAndRemoveDiscord(final DiscordCacheInfo cacheInfo) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            SocketConnection.sendRemove(cacheInfo);
            try (Connection connection = getConnection()) {
                PreparedStatement deleteUUID = connection.prepareStatement("SELECT * FROM discord WHERE minecraftUUID=?");
                deleteUUID.setString(1, cacheInfo.getMinecraft().getUuid());
                ResultSet result = deleteUUID.executeQuery();
                DiscordDatabaseInfo databaseInfo;
                if (!result.next()) {
                    result.close();
                    deleteUUID.close();
                } else {
                    boolean failed = false;
                    if (result.getString("minecraftUsername") == null) failed = true;
                    if (result.getString("minecraftUUID") == null) failed = true;
                    if (result.getString("discordUsername") == null) failed = true;
                    if (failed) {
                        result.close();
                        deleteUUID.close();
                    } else {
                        databaseInfo = new DiscordDatabaseInfo(result.getString("minecraftUsername"), result.getString("minecraftUUID"), result.getString("discordUsername"));
                        result.close();
                        deleteUUID.close();
                        if (databaseInfo != null) {
                            removeDiscord(databaseInfo);
                            DiscordCacheInfo info = new DiscordCacheInfo(new DiscordCacheInfo.Minecraft(databaseInfo.getMinecraftUsername(), databaseInfo.getMinecraftUUID(), ""),
                                    new DiscordCacheInfo.Discord(databaseInfo.getDiscordUsername()));
                            SocketConnection.sendRemove(info);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try (Connection connection = getConnection()) {
                PreparedStatement deleteUsername = connection.prepareStatement("SELECT * FROM discord WHERE discordUsername=?");
                deleteUsername.setString(1, cacheInfo.getDiscord().getUsername());
                ResultSet result = deleteUsername.executeQuery();
                DiscordDatabaseInfo databaseInfo;
                if (!result.next()) {
                    result.close();
                    deleteUsername.close();
                } else {
                    boolean failed = false;
                    if (result.getString("minecraftUsername") == null) failed = true;
                    if (result.getString("minecraftUUID") == null) failed = true;
                    if (result.getString("discordUsername") == null) failed = true;
                    if (failed) {
                        result.close();
                        deleteUsername.close();
                    } else {
                        databaseInfo = new DiscordDatabaseInfo(result.getString("minecraftUsername"), result.getString("minecraftUUID"), result.getString("discordUsername"));
                        result.close();
                        deleteUsername.close();
                        if (databaseInfo != null) {
                            removeDiscord(databaseInfo);
                            DiscordCacheInfo info = new DiscordCacheInfo(new DiscordCacheInfo.Minecraft(databaseInfo.getMinecraftUsername(), databaseInfo.getMinecraftUUID(), ""),
                                    new DiscordCacheInfo.Discord(databaseInfo.getDiscordUsername()));
                            SocketConnection.sendRemove(info);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            insertDiscord(cacheInfo);
        });
    }

    public void removeDiscord(final DiscordDatabaseInfo databaseInfo) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            try (Connection connection = getConnection()) {
                PreparedStatement deleteUUID = connection.prepareStatement("DELETE FROM discord WHERE minecraftUUID=?");
                deleteUUID.setString(1, databaseInfo.getMinecraftUUID());
                deleteUUID.execute();
                deleteUUID.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try (Connection connection = getConnection()) {
                PreparedStatement deleteUsername = connection.prepareStatement("DELETE FROM discord WHERE discordUsername=?");
                deleteUsername.setString(1, databaseInfo.getDiscordUsername());
                deleteUsername.execute();
                deleteUsername.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public DiscordCacheInfo getUserFromPlayer(final Player player) {
        try (Connection connection = getConnection()) {
            PreparedStatement useruuid = connection.prepareStatement("SELECT * FROM discord WHERE minecraftUUID=?");
            useruuid.setString(1, player.getUniqueId().toString());
            ResultSet result = useruuid.executeQuery();
            if (!result.next()) {
                result.close();
                useruuid.close();
            } else {
                if (result.getString("minecraftUsername") != null && result.getString("minecraftUUID") != null && result.getString("discordUsername") != null) {
                    DiscordCacheInfo info = new DiscordCacheInfo(new DiscordCacheInfo.Minecraft(player.getUsername(), player.getUniqueId().toString(), ""),
                            new DiscordCacheInfo.Discord(result.getString("discordUsername")));
                    result.close();
                    useruuid.close();
                    return info;
                }
                result.close();
                useruuid.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateProviderData(UUID uuid, IPUtil.ProviderData data) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET isp=?,country=?,region=?,regionName=?,timezone=? WHERE uuid=?");
            sql.setString(1, data.getIsp());
            sql.setString(2, data.getCountry());
            sql.setString(3, data.getRegion());
            sql.setString(4, data.getRegionName());
            sql.setString(5, data.getTimezone());
            sql.setString(6, uuid.toString());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Password Methods
     */

    public boolean verifyPassword(UUID uuid, String pass) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT password FROM staffpasswords WHERE uuid=?");
            sql.setString(1, uuid.toString());
            ResultSet result = sql.executeQuery();
            if (!result.next()) {
                result.close();
                sql.close();
                return false;
            }
            String hashed = result.getString("password");
            result.close();
            sql.close();
            return Launcher.getDashboard().getPasswordUtil().validPassword(pass, hashed);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasPassword(UUID uuid) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT password FROM staffpasswords WHERE uuid=?");
            sql.setString(1, uuid.toString());
            ResultSet result = sql.executeQuery();
            boolean has = result.next();
            result.close();
            sql.close();
            return has;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void changePassword(UUID uuid, String pass) {
        Dashboard dashboard = Launcher.getDashboard();
        String salt = dashboard.getPasswordUtil().getNewSalt();
        String hashed = dashboard.getPasswordUtil().hashPassword(pass, salt);
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("UPDATE staffpasswords SET password=? WHERE uuid=?");
            sql.setString(1, hashed);
            sql.setString(2, uuid.toString());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setPassword(UUID uuid, String pass) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO staffpasswords (uuid,password) VALUES (?,?)");
            sql.setString(1, uuid.toString());
            sql.setString(2, pass);
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}