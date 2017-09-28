package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.discordSocket.DiscordCacheInfo;
import network.palace.dashboard.discordSocket.DiscordDatabaseInfo;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.dashboard.PacketPlayerRank;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by Marc on 7/14/16
 */
public class SqlUtil {
    public void login(final Player player) {
        Dashboard dashboard = Launcher.getDashboard();

        dashboard.getSchedulerManager().runAsync(() -> {
            // Check if the uuid is from MCLeaks before we continue.
            boolean isMCLeaks = MCLeakUtil.checkPlayer(player);
            if (isMCLeaks) {
                // UUID is in MCLeaks, temp ban the account
                banPlayer(player.getUuid(), "MCLeaks Account", true, new Date(System.currentTimeMillis()), "Dashboard");
                dashboard.getModerationUtil().announceBan(new Ban(player.getUniqueId(), player.getUsername(),
                        true, System.currentTimeMillis(), "MCLeaks Account", "Dashboard"));
                player.kickPlayer(ChatColor.RED + "MCLeaks Accounts are not allowed on the Palace Network\n" +
                        ChatColor.AQUA + "If you think were banned incorrectly, submit an appeal at palnet.us/appeal");
            }
        });

        dashboard.getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement sql = connection.prepareStatement("SELECT rank,ipAddress,username,toggled,mentions,onlinetime,tutorial,mcversion FROM player_data WHERE uuid=?");
                sql.setString(1, player.getUniqueId().toString());
                ResultSet result = sql.executeQuery();
                if (!result.next()) {
                    newPlayer(player, connection);
                    result.close();
                    sql.close();
                    return;
                }
                long ot = result.getLong("onlinetime");
                player.setOnlineTime(ot == 0 ? 1 : ot);
                Rank rank = Rank.fromString(result.getString("rank"));
                if (!rank.equals(Rank.SETTLER)) {
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
                player.setNewGuest(result.getInt("tutorial") != 1);
                dashboard.addPlayer(player);
                dashboard.getPlayerLog().info("New Player Object for UUID " + player.getUniqueId() + " username " + player.getUsername() + " Source: SqlUtil.login");
                dashboard.addToCache(player.getUniqueId(), player.getUsername());

                String username = result.getString("username");
                int protocolVersion = result.getInt("mcversion");
                result.close();
                sql.close();
                if (needsUpdate)
                    update(player, connection, !player.getUsername().equals(username), player.getMcVersion() != protocolVersion);
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                            rank.getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked in.";
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
                    dashboard.getFriendUtil().friendMessage(player, friendList, joinMessage);
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
                ErrorUtil.logError("Error in SqlUtil with login method", e);
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
        dashboard.getPlayerLog().info("New Player Object for UUID " + player.getUniqueId() + " username " + player.getUsername() + " Source: SqlUtil.newPlayer");
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
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
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
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement statement = connection.prepareStatement("SELECT toggled,mentions,onlinetime FROM player_data WHERE uuid=?");
            statement.setString(1, player.getUniqueId().toString());
            ResultSet result = statement.executeQuery();
            if (!result.next()) {
                return;
            }
            player.setToggled(result.getInt("toggled") == 1);
            player.setMentions(result.getInt("mentions") == 1);
            long ot = result.getLong("onlinetime");
            player.setOnlineTime(ot == 0 ? 1 : ot);
            result.close();
            statement.close();
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
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET server=?,lastseen=?,onlinetime = onlinetime+? WHERE uuid=?");
                if (player.getServer() != null) {
                    statement.setString(1, player.getServer());
                } else {
                    statement.setString(1, "Unknown");
                }
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                statement.setInt(3, (int) ((System.currentTimeMillis() / 1000) - (player.getLoginTime() / 1000)));
                statement.setString(4, player.getUniqueId().toString());
                statement.execute();
                statement.close();

                Rank rank = player.getRank();
                if (rank.getRankId() >= Rank.CHARACTER.getRankId()) {
                    String msg = ChatColor.WHITE + "[" + ChatColor.RED + "STAFF" + ChatColor.WHITE + "] " +
                            rank.getFormattedName() + " " + ChatColor.YELLOW + player.getUsername() + " has clocked out.";
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getRank().getRankId() >= Rank.SQUIRE.getRankId()) {
                            tp.sendMessage(msg);
                        }
                    }
                    staffClock(player.getUniqueId(), false, connection);
                }
                HashMap<UUID, String> flist = player.getFriends();
                if (!flist.isEmpty()) {
                    String msg = rank.getTagColor() + player.getUsername() + ChatColor.LIGHT_PURPLE + " has left.";
                    dashboard.getFriendUtil().friendMessage(player, flist, msg);
                }
            } catch (SQLException e) {
                ErrorUtil.logError("Error in SqlUtil method logout", e);
            }
        });
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
        if (uuid == null) {
            return map;
        }
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return new HashMap<>();
        }
        try (Connection connection = optConnection.get()) {
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
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
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
    public void banPlayer(final Ban ban) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO banned_players (uuid,reason,permanent,`release`,source) VALUES (?,?,?,?,?)");
                statement.setString(1, ban.getUniqueId().toString());
                statement.setString(2, ban.getReason());
                statement.setInt(3, ban.isPermanent() ? 1 : 0);
                statement.setTimestamp(4, new Timestamp(new Date(ban.getRelease()).getTime()));
                statement.setString(5, ban.getSource());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void banPlayer(final UUID uuid, final String reason, final boolean permanent, final Date date, final String source) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO banned_players (uuid,reason,permanent,`release`,source) VALUES (?,?,?,?,?)");
                statement.setString(1, uuid.toString());
                statement.setString(2, reason);
                statement.setInt(3, permanent ? 1 : 0);
                statement.setTimestamp(4, new Timestamp(date.getTime()));
                statement.setString(5, source);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isBannedPlayer(UUID uuid) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return false;
        }
        try (Connection connection = optConnection.get()) {
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
            ErrorUtil.logError("SQL Error is banned player method", e);
            return false;
        }
    }

    public void banIP(final String ip, final String reason, final String source) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO banned_ips VALUES(0,?,?,?,1)");
                statement.setString(1, ip);
                statement.setString(2, reason);
                statement.setString(3, source);
                statement.execute();
                statement.close();
            } catch (Exception e) {
                ErrorUtil.logError("SQL Error ban ip method", e);
            }
        });
    }

    public void banProvider(final ProviderBan ban) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO banned_providers VALUES (0,?,?)");
                statement.setString(1, ban.getProvider());
                statement.setString(2, ban.getSource());
                statement.execute();
                statement.close();
            } catch (Exception e) {
                ErrorUtil.logError("SQL Error ban provider method", e);
            }
        });
    }

    public Ban getBan(UUID uuid, String name) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return null;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement statement = connection.prepareStatement("SELECT permanent,`release`,reason,source FROM banned_players WHERE uuid=? AND active=1;");
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            Ban ban;
            if (!result.next()) {
                return null;
            }
            ban = new Ban(uuid, name, result.getInt("permanent") == 1, result.getTimestamp("release").getTime(),
                    result.getString("reason"), result.getString("source"));
            result.close();
            statement.close();
            return ban;
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error get ban method", e);
            return null;
        }
    }

    public AddressBan getAddressBan(String address) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return null;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement statement = connection.prepareStatement("SELECT ipAddress,reason,source FROM banned_ips WHERE ipAddress=? AND active=1;");
            statement.setString(1, address);
            ResultSet result = statement.executeQuery();
            AddressBan ban;
            if (!result.next()) {
                return null;
            }
            ban = new AddressBan(result.getString("ipAddress"), result.getString("reason"), result.getString("source"));
            result.close();
            statement.close();
            return ban;
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error get address ban method", e);
            return null;
        }
    }

    public ProviderBan getProviderBan(String provider) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return null;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement sql = connection.prepareStatement("SELECT provider,source FROM banned_providers WHERE provider=? AND active=1;");
            sql.setString(1, provider);
            ResultSet result = sql.executeQuery();
            ProviderBan ban;
            if (!result.next()) {
                return null;
            }
            ban = new ProviderBan(result.getString("provider"), result.getString("source"));
            result.close();
            sql.close();
            return ban;
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error get provider ban method", e);
            return null;
        }
    }

    public List<String> getBannedProviders() {
        List<String> list = new ArrayList<>();
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return new ArrayList<>();
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement statement = connection.prepareStatement("SELECT provider FROM banned_providers WHERE active=1");
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                list.add(result.getString("provider"));
            }
            result.close();
            statement.close();
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error get banned providers method", e);
        }
        return list;
    }

    public void unbanPlayer(final UUID uuid) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE banned_players SET active=0 WHERE uuid=?");
                statement.setString(1, uuid.toString());
                statement.execute();
                statement.close();
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error unban player method", e);
            }
        });
    }

    public void unbanIP(final String address) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE banned_ips SET active=0 WHERE ipAddress=?");
                statement.setString(1, address);
                statement.execute();
                statement.close();
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error unban ip method", e);
            }
        });
    }

    public void unbanProvider(final String provider) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE banned_providers SET active=0 WHERE provider=?");
                statement.setString(1, provider);
                statement.execute();
                statement.close();
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error unban provider method", e);
            }
        });
    }

    /**
     * Mute Methods
     */

    public Mute getMute(UUID uuid, String username) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return null;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement statement = connection.prepareStatement("SELECT reason,`release`,source,active FROM muted_players WHERE uuid=?");
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            Mute mute = null;
            while (result.next()) {
                if (result.getInt("active") == 1) {
                    mute = new Mute(uuid, username, true, result.getTimestamp("release").getTime(),
                            result.getString("reason"), result.getString("source"));
                }
            }
            if (mute == null) {
                result.close();
                statement.close();
                return new Mute(uuid, username, false, System.currentTimeMillis(), "", "");
            }
            result.close();
            statement.close();
            return mute;
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error get mute method", e);
            return null;
        }
    }

    public void mutePlayer(Mute mute) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO muted_players (uuid,`release`,source,reason) VALUES (?,?,?,?)");
            statement.setString(1, mute.getUniqueId().toString());
            statement.setTimestamp(2, new Timestamp(mute.getRelease()));
            statement.setString(3, mute.getSource());
            statement.setString(4, mute.getReason());
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error mute player method", e);
        }
    }

    public boolean isMuted(UUID uuid) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return false;
        }
        try (Connection connection = optConnection.get()) {
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
            ErrorUtil.logError("SQL Error is muted method", e);
        }
        return false;
    }

    public void unmutePlayer(final UUID uuid) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE muted_players SET active=0 WHERE uuid=?");
                statement.setString(1, uuid.toString());
                statement.execute();
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error unmute player method", e);
            }
        });
    }

    /**
     * Kick Methods
     */

    public void logKick(final Kick kick) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement sql = connection.prepareStatement("INSERT INTO kicks (uuid, reason, source) VALUES (?,?,?)");
                sql.setString(1, kick.getUniqueId().toString());
                sql.setString(2, kick.getReason());
                sql.setString(3, kick.getSource());
                sql.execute();
                sql.close();
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error log kick method", e);
            }
        });
    }

    /**
     * Discord Methods
     */

    public void insertDiscord(final DiscordCacheInfo cacheInfo) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO discord (minecraftUsername, minecraftUUID, discordUsername) VALUES (?,?,?)");
                statement.setString(1, cacheInfo.getMinecraft().getUsername());
                statement.setString(2, cacheInfo.getMinecraft().getUuid());
                statement.setString(3, cacheInfo.getDiscord().getUsername());
                statement.execute();
                statement.close();
                SocketConnection.sendNewlink(cacheInfo);
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error insert discord method", e);
            }
        });
    }

    public void selectAndRemoveDiscord(final DiscordCacheInfo cacheInfo) {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getSchedulerManager().runAsync(() -> {
            SocketConnection.sendRemove(cacheInfo);
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM discord WHERE minecraftUUID=?");
                statement.setString(1, cacheInfo.getMinecraft().getUuid());
                ResultSet result = statement.executeQuery();
                DiscordDatabaseInfo databaseInfo;
                if (!result.next()) {
                    result.close();
                    statement.close();
                } else {
                    boolean failed = false;
                    if (result.getString("minecraftUsername") == null) failed = true;
                    if (result.getString("minecraftUUID") == null) failed = true;
                    if (result.getString("discordUsername") == null) failed = true;
                    if (failed) {
                        result.close();
                        statement.close();
                    } else {
                        databaseInfo = new DiscordDatabaseInfo(result.getString("minecraftUsername"), result.getString("minecraftUUID"), result.getString("discordUsername"));
                        result.close();
                        statement.close();
                        removeDiscord(databaseInfo);
                        DiscordCacheInfo info = new DiscordCacheInfo(new DiscordCacheInfo.Minecraft(databaseInfo.getMinecraftUsername(), databaseInfo.getMinecraftUUID(), ""),
                                new DiscordCacheInfo.Discord(databaseInfo.getDiscordUsername()));
                        SocketConnection.sendRemove(info);
                    }
                }
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error select and remove discord method 1", e);
            }
            optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM discord WHERE discordUsername=?");
                statement.setString(1, cacheInfo.getDiscord().getUsername());
                ResultSet result = statement.executeQuery();
                DiscordDatabaseInfo databaseInfo;
                if (!result.next()) {
                    result.close();
                    statement.close();
                } else {
                    boolean failed = false;
                    if (result.getString("minecraftUsername") == null) failed = true;
                    if (result.getString("minecraftUUID") == null) failed = true;
                    if (result.getString("discordUsername") == null) failed = true;
                    if (failed) {
                        result.close();
                        statement.close();
                    } else {
                        databaseInfo = new DiscordDatabaseInfo(result.getString("minecraftUsername"), result.getString("minecraftUUID"), result.getString("discordUsername"));
                        result.close();
                        statement.close();
                        removeDiscord(databaseInfo);
                        DiscordCacheInfo info = new DiscordCacheInfo(new DiscordCacheInfo.Minecraft(databaseInfo.getMinecraftUsername(), databaseInfo.getMinecraftUUID(), ""),
                                new DiscordCacheInfo.Discord(databaseInfo.getDiscordUsername()));
                        SocketConnection.sendRemove(info);
                    }
                }
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error select and remove discord method 2", e);
            }
            insertDiscord(cacheInfo);
        });
    }

    public void removeDiscord(final DiscordDatabaseInfo databaseInfo) {
        Launcher.getDashboard().getSchedulerManager().runAsync(() -> {
            Optional<Connection> optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement deleteUUID = connection.prepareStatement("DELETE FROM discord WHERE minecraftUUID=?");
                deleteUUID.setString(1, databaseInfo.getMinecraftUUID());
                deleteUUID.execute();
                deleteUUID.close();
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error remove discord method 1", e);
            }
            optConnection = getConnection();
            if (!optConnection.isPresent()) {
                ErrorUtil.logError("Unable to connect to mysql");
                return;
            }
            try (Connection connection = optConnection.get()) {
                PreparedStatement deleteUsername = connection.prepareStatement("DELETE FROM discord WHERE discordUsername=?");
                deleteUsername.setString(1, databaseInfo.getDiscordUsername());
                deleteUsername.execute();
                deleteUsername.close();
            } catch (SQLException e) {
                ErrorUtil.logError("SQL Error remove discord method 2", e);
            }
        });
    }

    public DiscordCacheInfo getUserFromPlayer(final Player player) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return null;
        }
        try (Connection connection = optConnection.get()) {
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
            ErrorUtil.logError("SQL Error get user from player method", e);
        }
        return null;
    }

    public void updateProviderData(UUID uuid, IPUtil.ProviderData data) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE player_data SET isp=?,country=?,region=?,regionName=?,timezone=? WHERE uuid=?");
            statement.setString(1, data.getIsp());
            statement.setString(2, data.getCountry());
            statement.setString(3, data.getRegion());
            statement.setString(4, data.getRegionName());
            statement.setString(5, data.getTimezone());
            statement.setString(6, uuid.toString());
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error update provider data method", e);
        }
    }


    /**
     * Password Methods
     */

    public boolean verifyPassword(UUID uuid, String pass) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return false;
        }
        try (Connection connection = optConnection.get()) {
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
            ErrorUtil.logError("SQL Error verify password method", e);
        }
        return false;
    }

    public boolean hasPassword(UUID uuid) {
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return false;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement sql = connection.prepareStatement("SELECT password FROM staffpasswords WHERE uuid=?");
            sql.setString(1, uuid.toString());
            ResultSet result = sql.executeQuery();
            boolean has = result.next();
            result.close();
            sql.close();
            return has;
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error has password method", e);
        }
        return false;
    }

    public void changePassword(UUID uuid, String pass) {
        Dashboard dashboard = Launcher.getDashboard();
        String salt = dashboard.getPasswordUtil().getNewSalt();
        String hashed = dashboard.getPasswordUtil().hashPassword(pass, salt);
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement sql = connection.prepareStatement("UPDATE staffpasswords SET password=? WHERE uuid=?");
            sql.setString(1, hashed);
            sql.setString(2, uuid.toString());
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error change password method", e);
        }
    }

    public void setPassword(UUID uuid, String pass) {
        Dashboard dashboard = Launcher.getDashboard();
        String salt = dashboard.getPasswordUtil().getNewSalt();
        String hashed = dashboard.getPasswordUtil().hashPassword(pass, salt);
        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO staffpasswords (uuid,password) VALUES (?,?)");
            sql.setString(1, uuid.toString());
            sql.setString(2, hashed);
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            ErrorUtil.logError("SQL Error set password method", e);
        }
    }
}