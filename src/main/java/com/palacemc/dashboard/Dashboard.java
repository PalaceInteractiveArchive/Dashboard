package com.palacemc.dashboard;

import com.palacemc.dashboard.handlers.Player;
import com.palacemc.dashboard.handlers.Rank;
import com.palacemc.dashboard.handlers.Server;
import com.palacemc.dashboard.packets.audio.PacketContainer;
import com.palacemc.dashboard.packets.audio.PacketKick;
import com.palacemc.dashboard.packets.dashboard.PacketConnectionType;
import com.palacemc.dashboard.scheduler.SchedulerManager;
import com.palacemc.dashboard.server.DashboardSocketChannel;
import com.palacemc.dashboard.server.WebSocketServerHandler;
import com.palacemc.dashboard.utils.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Dashboard {
    @Getter private final int PORT = 7892;
    @Getter @Setter private SqlUtil sqlUtil = null;
    @Getter @Setter private ServerUtil serverUtil = null;
    @Getter @Setter private ChatUtil chatUtil = null;
    @Getter @Setter private CommandUtil commandUtil = null;
    @Getter @Setter private ModerationUtil moderationUtil = new ModerationUtil();
    @Getter @Setter private FriendUtil friendUtil;
    @Getter @Setter private ActivityUtil activityUtil;
    @Getter @Setter private PartyUtil partyUtil;
    @Getter @Setter private SlackUtil slackUtil;
    @Getter @Setter private WarningUtil warningUtil;
    @Getter @Setter private SiteUtil siteUtil;
    @Getter @Setter  private AFKUtil afkUtil;
    @Getter @Setter private StatUtil statUtil;
    @Getter @Setter private SchedulerManager schedulerManager = null;
    @Getter private Logger logger = Logger.getLogger("Dashboard");

    @Getter @Setter private long startTime;

    private List<String> serverTypes = new ArrayList<>();
    private HashMap<UUID, Player> players = new HashMap<>();
    private HashMap<UUID, String> cache = new HashMap<>();
    @Getter @Setter private String motd = "";
    @Getter @Setter private String motdMaintenance = "";
    @Getter private List<String> info = new ArrayList<>();
    @Getter @Setter private String targetServer = "unknown";
    @Getter private List<String> joinServers = new ArrayList<>();
    @Getter @Setter private boolean maintenance = false;
    @Getter @Setter private List<UUID> maintenanceWhitelist = new ArrayList<>();
    @Getter private boolean testNetwork = false;

    public void loadConfiguration() {
        try (BufferedReader br = new BufferedReader(new FileReader("config.txt"))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("maintenance:")) {
                    maintenance = Boolean.parseBoolean(line.split("maintenance:")[1]);
                } else if (line.startsWith("test-network:")) {
                    testNetwork = Boolean.parseBoolean(line.split("test-network:")[1]);
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (maintenance) {
            maintenanceWhitelist.clear();
            HashMap<Rank, List<UUID>> staff = getSqlUtil().getPlayersByRanks(Rank.SQUIRE, Rank.ARCHITECT,
                    Rank.KNIGHT, Rank.PALADIN, Rank.WIZARD, Rank.EMPEROR, Rank.EMPRESS);

            for (Map.Entry<Rank, List<UUID>> entry : staff.entrySet()) {
                for (UUID uuid : entry.getValue()) {
                    maintenanceWhitelist.add(uuid);
                }
            }
        }
    }

    public void loadMOTD() {
        info.clear();

        try (BufferedReader br = new BufferedReader(new FileReader("motd.txt"))) {
            String line = br.readLine();
            boolean isInfo = false;
            while (line != null) {
                if (line.startsWith("motd:")) {
                    motd = line.split("motd:")[1];
                } else if (line.startsWith("maintenance:")) {
                    motdMaintenance = line.split("maintenance:")[1];
                } else if (line.startsWith("info:")) {
                    isInfo = true;
                } else if (isInfo) {
                    info.add(line);
                }
                line = br.readLine();
            }

            motd = motd.replaceAll("%n%", System.getProperty("line.separator"));
            motdMaintenance = motdMaintenance.replaceAll("%n%", System.getProperty("line.separator"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadServerTypes() {
        serverTypes.clear();

        try (Connection connection = sqlUtil.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT name FROM servertypes");
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                serverTypes.add(result.getString("name"));
            }

            result.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void loadJoinServers() {
        joinServers.clear();

        try (BufferedReader br = new BufferedReader(new FileReader("servers.txt"))) {
            String line = br.readLine();
            while (line != null) {
                String s = "";
                if (line.startsWith("- ")) {
                    s = line.split("- ")[1];
                }
                joinServers.add(s);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToCache(UUID uuid, String name) {
        cache.put(uuid, name);
    }

    public String getCachedName(UUID uuid) {
        return cache.get(uuid);
    }

    public List<DashboardSocketChannel> getChannels(PacketConnectionType.ConnectionType type) {
        List<DashboardSocketChannel> list = new ArrayList<>();
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (dash.getType().equals(type)) {
                list.add(dash);
            }
        }
        return list;
    }

    public Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public Player getPlayer(String username) {
        for (Player player : players.values()) {
            if (player.getName().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    public List<Player> getOnlinePlayers() {
        return new ArrayList<>(players.values());
    }

    public void addPlayer(Player player) {
        players.put(player.getUniqueId(), player);
    }

    public Server getServer(String server) {
        Server s = serverUtil.getServer(server);
        if (s == null) {
            return serverUtil.getServer(targetServer);
        }
        return s;
    }

    public DashboardSocketChannel getBungee(UUID bungeeID) {
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                continue;
            }
            if (dash.getBungeeID().equals(bungeeID)) {
                return dash;
            }
        }
        return null;
    }

    public DashboardSocketChannel getInstance(String name) {
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                continue;
            }
            if (dash.getServerName().equalsIgnoreCase(name)) {
                return dash;
            }
        }
        return null;
    }

    public void logout(UUID uuid) {
        Player player = getPlayer(uuid);

        if (player != null) {
            if (!player.getServer().equalsIgnoreCase("unknown")) {
                getServerUtil().getServer(player.getServer()).changeCount(-1);
            }
            if (player.getTutorial() != null) {
                player.getTutorial().cancel();
            }
            sqlUtil.logout(player);
        }

        PacketKick packet = new PacketKick("See ya real soon!");
        PacketContainer kick = new PacketContainer(uuid, packet.getJSON().toString());

        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.AUDIOSERVER)) {
                continue;
            }
            dash.send(kick);
        }

        players.remove(uuid);
    }
}