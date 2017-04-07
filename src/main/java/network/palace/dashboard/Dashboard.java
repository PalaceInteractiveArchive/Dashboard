package network.palace.dashboard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.forums.Forum;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.Server;
import network.palace.dashboard.packets.audio.PacketContainer;
import network.palace.dashboard.packets.audio.PacketHeartbeat;
import network.palace.dashboard.packets.audio.PacketKick;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.scheduler.SchedulerManager;
import network.palace.dashboard.scheduler.ShowReminder;
import network.palace.dashboard.server.DashboardServerSocketChannel;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;
import network.palace.dashboard.server.WebSocketServerInitializer;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;
import network.palace.dashboard.utils.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Dashboard {
    public static final int PORT = 7892;
    public static String HOST;
    public static SqlUtil sqlUtil = null;
    public static ServerUtil serverUtil = null;
    public static ChatUtil chatUtil = null;
    public static CommandUtil commandUtil = null;
    public static ModerationUtil moderationUtil = new ModerationUtil();
    public static SchedulerManager schedulerManager = null;
    public static FriendUtil friendUtil;
    public static ActivityUtil activityUtil;
    public static PartyUtil partyUtil;
    public static SlackUtil slackUtil;
    public static WarningUtil warningUtil;
    public static SiteUtil siteUtil;
    public static AFKUtil afkUtil;
    public static StatUtil statUtil;
    public static VoteUtil voteUtil;
    public static Forum forum;
    @Getter private static Random random;
    private static Logger logger = Logger.getLogger("Dashboard");
    private static List<String> serverTypes = new ArrayList<>();
    private static HashMap<UUID, Player> players = new HashMap<>();
    private static HashMap<UUID, String> cache = new HashMap<>();
    private static String motd = "";
    private static String motdmaintenance = "";
    private static List<String> info = new ArrayList<>();
    private static String targetServer = "unknown";
    private static List<String> joinServers = new ArrayList<>();
    private static long startTime;
    private static boolean maintenance = false;
    private static List<UUID> maintenanceWhitelist = new ArrayList<>();
    private static boolean testNetwork = false;

    @Getter private static String socketURL = "";
    @Getter private static SocketConnection socketConnection;

    public static void main(String[] args) throws IOException {
        startTime = System.currentTimeMillis();
        PatternLayout layout = new PatternLayout("[%d{HH:mm:ss}] [%p] - %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, "dashboard.log", true));
        getLogger().info("Starting up Dashboard...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Dashboard.getLogger().warn("Shutting down Dashboard...");
            voteUtil.stop();
            slackUtil.sendDashboardMessage(new SlackMessage(),
                    Arrays.asList(new SlackAttachment("Dashboard went offline! #devs").color("danger")));
        }));
        random = new Random();
        schedulerManager = new SchedulerManager();
        try {
            logger.info("Initializing SQL Connections");
            sqlUtil = new SqlUtil();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        loadConfiguration();
        loadMOTD();
        loadServerTypes();
        loadJoinServers();
        if (testNetwork) {
            getLogger().info("Test network detected, disabling statistics collection!");
        }
        socketConnection = new SocketConnection();

        serverUtil = new ServerUtil();
        chatUtil = new ChatUtil();
        commandUtil = new CommandUtil();
        partyUtil = new PartyUtil();
        slackUtil = new SlackUtil();
        warningUtil = new WarningUtil();
        try {
            siteUtil = new SiteUtil();
        } catch (Exception e) {
            e.printStackTrace();
        }
        afkUtil = new AFKUtil();
        statUtil = new StatUtil();
        voteUtil = new VoteUtil();
        try {
            forum = new Forum();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setupShowReminder();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                PacketHeartbeat packet = new PacketHeartbeat();
                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    dash.send(packet);
                }
            }
        }, 10 * 1000, 30 * 1000);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(DashboardServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer());
            Channel ch = b.bind(new InetSocketAddress(HOST, PORT)).sync().channel();
            getLogger().info("Dashboard started at " + HOST + ":" + PORT);
            slackUtil.sendDashboardMessage(new SlackMessage(),
                    Arrays.asList(new SlackAttachment("Dashboard has successfully started up!").color("good")));
            ch.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static void loadConfiguration() {
        try (BufferedReader br = new BufferedReader(new FileReader("config.txt"))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("host:")) {
                    HOST = line.split("host:")[1];
                } else if (line.startsWith("maintenance:")) {
                    maintenance = Boolean.parseBoolean(line.split("maintenance:")[1]);
                } else if (line.startsWith("test-network:")) {
                    testNetwork = Boolean.parseBoolean(line.split("test-network:")[1]);
                } else if (line.startsWith("socketURL:")) {
                    socketURL = line.split("socketURL:")[1];
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (maintenance) {
            maintenanceWhitelist.clear();
            HashMap<Rank, List<UUID>> staff = Dashboard.sqlUtil.getPlayersByRanks(Rank.SQUIRE, Rank.ARCHITECT,
                    Rank.KNIGHT, Rank.PALADIN, Rank.WIZARD, Rank.EMPEROR, Rank.EMPRESS);
            for (Map.Entry<Rank, List<UUID>> entry : staff.entrySet()) {
                maintenanceWhitelist.addAll(entry.getValue());
            }
        }
    }

    public static void loadMOTD() {
        info.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("motd.txt"))) {
            String line = br.readLine();
            boolean isInfo = false;
            while (line != null) {
                if (line.startsWith("motd:")) {
                    motd = line.split("motd:")[1];
                } else if (line.startsWith("maintenance:")) {
                    motdmaintenance = line.split("maintenance:")[1];
                } else if (line.startsWith("info:")) {
                    isInfo = true;
                } else if (isInfo) {
                    info.add(line);
                }
                line = br.readLine();
            }
            motd = motd.replaceAll("%n%", System.getProperty("line.separator"));
            motdmaintenance = motdmaintenance.replaceAll("%n%", System.getProperty("line.separator"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadServerTypes() {
        serverTypes.clear();
        try (Connection connection = sqlUtil.getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT name FROM servertypes");
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                serverTypes.add(result.getString("name"));
            }
            result.close();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void loadJoinServers() {
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

    public static void addToCache(UUID uuid, String name) {
        cache.put(uuid, name);
    }

    public static String getCachedName(UUID uuid) {
        return cache.get(uuid);
    }

    public static List<String> getServerTypes() {
        return new ArrayList<>(serverTypes);
    }

    public static Logger getLogger() {
        return logger;
    }

    public static List<DashboardSocketChannel> getChannels(PacketConnectionType.ConnectionType type) {
        List<DashboardSocketChannel> list = new ArrayList<>();
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (dash.getType().equals(type)) {
                list.add(dash);
            }
        }
        return list;
    }

    public static Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public static Player getPlayer(String username) {
        for (Player player : players.values()) {
            if (player.getName().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    public static Server getServer(String server) {
        return getServer(server, false);
    }

    public static Server getServer(String server, boolean noTarget) {
        Server s = serverUtil.getServer(server);
        if (s == null && !noTarget) {
            return serverUtil.getServer(targetServer);
        }
        return s;
    }

    public static List<Server> getServers() {
        return serverUtil.getServers();
    }

    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(players.values());
    }

    public static void addPlayer(Player player) {
        players.put(player.getUniqueId(), player);
    }

    public static DashboardSocketChannel getBungee(UUID bungeeID) {
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

    public static DashboardSocketChannel getInstance(String name) {
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

    public static void logout(UUID uuid) {
        Player player = getPlayer(uuid);
        if (player != null) {
            if (!player.getServer().equalsIgnoreCase("unknown")) {
                Dashboard.serverUtil.getServer(player.getServer()).changeCount(-1);
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

    public static String getMOTD() {
        return motd;
    }

    public static void setMOTD(String motd) {
        Dashboard.motd = motd;
    }

    public static String getMOTDMaintenance() {
        return motdmaintenance;
    }

    public static void setMOTDMaintenance(String motd) {
        Dashboard.motdmaintenance = motd;
    }

    public static List<String> getInfo() {
        return info;
    }

    public static void setInfo(List<String> info) {
        Dashboard.info = info;
    }

    public static String getTargetServer() {
        return targetServer;
    }

    public static void setTargetServer(String targetServer) {
        Dashboard.targetServer = targetServer;
    }

    public static List<String> getJoinServers() {
        return new ArrayList<>(joinServers);
    }

    public static long getStartTime() {
        return startTime;
    }

    public static boolean isMaintenance() {
        return maintenance;
    }

    public static void setMaintenance(boolean maintenance) {
        Dashboard.maintenance = maintenance;
    }

    public static List<UUID> getMaintenanceWhitelist() {
        return maintenanceWhitelist;
    }

    public static void setMaintenanceWhitelist(List<UUID> list) {
        Dashboard.maintenanceWhitelist = list;
    }

    public static boolean isTestNetwork() {
        return testNetwork;
    }

    public static void setTestNetwork(boolean testNetwork) {
        Dashboard.testNetwork = testNetwork;
    }

    private static void setupShowReminder() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.of("America/New_York");
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNext11 = zonedNow.withHour(10).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext11_2 = zonedNow.withHour(10).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext11_3 = zonedNow.withHour(10).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext16 = zonedNow.withHour(15).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext16_2 = zonedNow.withHour(15).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext16_3 = zonedNow.withHour(15).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext21 = zonedNow.withHour(20).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext21_2 = zonedNow.withHour(20).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext21_3 = zonedNow.withHour(20).withMinute(40).withSecond(0);
        if (zonedNow.compareTo(zonedNext11) > 0) {
            zonedNext11 = zonedNext11.plusDays(1);
        }
        if (zonedNow.compareTo(zonedNext11_2) > 0) {
            zonedNext11_2 = zonedNext11_2.plusDays(1);
        }
        if (zonedNow.compareTo(zonedNext11_3) > 0) {
            zonedNext11_3 = zonedNext11_3.plusDays(1);
        }
        if (zonedNow.compareTo(zonedNext16) > 0) {
            zonedNext16 = zonedNext16.plusDays(1);
        }
        if (zonedNow.compareTo(zonedNext16_2) > 0) {
            zonedNext16_2 = zonedNext16_2.plusDays(1);
        }
        if (zonedNow.compareTo(zonedNext16_3) > 0) {
            zonedNext16_3 = zonedNext16_3.plusDays(1);
        }
        if (zonedNow.compareTo(zonedNext21) > 0) {
            zonedNext21 = zonedNext21.plusDays(1);
        }
        if (zonedNow.compareTo(zonedNext21_2) > 0) {
            zonedNext21_2 = zonedNext21_2.plusDays(1);
        }
        if (zonedNow.compareTo(zonedNext21_3) > 0) {
            zonedNext21_3 = zonedNext21_3.plusDays(1);
        }
        long d1 = Duration.between(zonedNow, zonedNext11).getSeconds();
        long d2 = Duration.between(zonedNow, zonedNext11_2).getSeconds();
        long d3 = Duration.between(zonedNow, zonedNext11_3).getSeconds();
        long d4 = Duration.between(zonedNow, zonedNext16).getSeconds();
        long d5 = Duration.between(zonedNow, zonedNext16_2).getSeconds();
        long d6 = Duration.between(zonedNow, zonedNext16_3).getSeconds();
        long d7 = Duration.between(zonedNow, zonedNext21).getSeconds();
        long d8 = Duration.between(zonedNow, zonedNext21_2).getSeconds();
        long d9 = Duration.between(zonedNow, zonedNext21_3).getSeconds();
        ScheduledExecutorService sch = Executors.newScheduledThreadPool(1);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 11am Show in 40 minutes!"), d1,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 11am Show in 30 minutes!"), d2,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 11am Show in 20 minutes!"), d3,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 4pm Show in 40 minutes!"), d4,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 4pm Show in 30 minutes!"), d5,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 4pm Show in 20 minutes!"), d6,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 9pm Show in 40 minutes!"), d7,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 9pm Show in 30 minutes!"), d8,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 9pm Show in 20 minutes!"), d9,
                24 * 60 * 60, TimeUnit.SECONDS);
    }

    public static String getRandomToken() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char c = chars[getRandom().nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }
}
