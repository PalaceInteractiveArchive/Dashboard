package network.palace.dashboard;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.forums.Forum;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.handlers.Rank;
import network.palace.dashboard.handlers.Server;
import network.palace.dashboard.handlers.SocketCallback;
import network.palace.dashboard.mongo.MongoHandler;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.audio.PacketContainer;
import network.palace.dashboard.packets.audio.PacketKick;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.queues.ParkQueueManager;
import network.palace.dashboard.scheduler.SchedulerManager;
import network.palace.dashboard.scheduler.ShowReminder;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;
import network.palace.dashboard.utils.*;
import network.palace.dashboard.utils.chat.JaroWinkler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Dashboard {
    @Getter public final String version;
    @Getter public final int PORT = 7892;
    @Getter @Setter public String HOST;

    @Getter @Setter private MongoHandler mongoHandler;
    @Getter @Setter private ServerUtil serverUtil = null;
    @Getter @Setter private ChatUtil chatUtil = null;
    @Getter @Setter private CommandUtil commandUtil = null;
    @Getter @Setter private EmojiUtil emojiUtil = null;
    @Getter @Setter private ModerationUtil moderationUtil = null;
    @Getter @Setter private SchedulerManager schedulerManager = null;
    @Getter @Setter private FriendUtil friendUtil;
    @Getter @Setter private PartyUtil partyUtil;
    @Getter @Setter private SlackUtil slackUtil;
    @Getter @Setter private WarningUtil warningUtil;
    @Getter @Setter private SiteUtil siteUtil;
    @Getter @Setter private AFKUtil afkUtil;
    @Getter @Setter private StatUtil statUtil;
    @Getter @Setter private VoteUtil voteUtil;
    //    @Getter @Setter private SqlUtil sqlUtil;
    @Getter @Setter private PasswordUtil passwordUtil;
    @Getter @Setter private InventoryUtil inventoryUtil;
    @Getter @Setter private ShowUtil showUtil;
    @Getter @Setter private GuideUtil guideUtil;
    @Getter @Setter private ParkQueueManager parkQueueManager;

    @Getter @Setter private boolean strictMode;
    @Getter @Setter private double strictThreshold;
    @Getter private JaroWinkler chatAlgorithm = new JaroWinkler();

    @Getter private String discordSocketURL = "";

    @Getter @Setter private Forum forum;
    @Getter @Setter private Random random;
    @Getter @Setter private Logger logger = LoggerFactory.getLogger(Dashboard.class);
    private HashMap<UUID, String> registering = new HashMap<>();
    @Getter @Setter private HashMap<UUID, Player> players = new HashMap<>();
    @Getter @Setter private HashMap<UUID, String> cache = new HashMap<>();
    @Getter @Setter private String motd = "";
    @Getter @Setter private String motdMaintenance = "";
    @Getter @Setter private List<String> info = new ArrayList<>();
    @Getter @Setter private String targetServer = "unknown";
    @Setter private List<String> joinServers = new ArrayList<>();
    @Getter @Setter private long startTime;
    @Getter @Setter private boolean maintenance = false;
    @Getter @Setter private List<UUID> maintenanceWhitelist = new ArrayList<>();
    @Getter @Setter private boolean testNetwork = false;

    public Dashboard() {
        version = getClass().getPackage().getImplementationVersion();
    }

    public void loadConfiguration() {
        try (BufferedReader br = new BufferedReader(new FileReader("config.txt"))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("host:")) {
                    HOST = line.split("host:")[1];
                } else if (line.startsWith("maintenance:")) {
                    maintenance = Boolean.parseBoolean(line.split("maintenance:")[1]);
                } else if (line.startsWith("test-network:")) {
                    testNetwork = Boolean.parseBoolean(line.split("test-network:")[1]);
                } else if (line.startsWith("discordSocketURL:")) {
                    discordSocketURL = line.split("discordSocketURL:")[1];
                }
                line = br.readLine();
            }
        } catch (IOException e) {
            logger.error("Error loading dashboard config", e);
        }
    }

    public void loadMaintenanceSettings() {
        if (maintenance) {
            maintenanceWhitelist.clear();
            logger.info("Loading list of staff members for maintenance mode...");
            List<UUID> staff = mongoHandler.getPlayersByRank(Rank.TRAINEE, Rank.TRAINEEBUILD, Rank.TRAINEETECH, Rank.MOD,
                    Rank.MEDIA, Rank.TECHNICIAN, Rank.BUILDER, Rank.ARCHITECT, Rank.COORDINATOR, Rank.DEVELOPER, Rank.LEAD,
                    Rank.MANAGER, Rank.DIRECTOR, Rank.OWNER);
            maintenanceWhitelist.addAll(staff);
            logger.info("Finished loading staff member list for maintenance mode!");
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
            logger.error("Error loading motd", e);
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
            logger.error("Error loading join servers", e);
        }
    }

    public void addToCache(UUID uuid, String name) {
        cache.put(uuid, name);
    }

    public String getCachedName(UUID uuid) {
        return cache.get(uuid);
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

    public Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public Player getPlayer(String username) {
        for (Player player : players.values()) {
            if (player.getUsername().equalsIgnoreCase(username)) {
                return player;
            }
        }
        return null;
    }

    public Server getServer(String server) {
        return getServer(server, false);
    }

    public Server getServer(String server, boolean noTarget) {
        Server s = serverUtil.getServer(server);
        if (s == null && !noTarget) {
            return serverUtil.getServer(targetServer);
        }
        return s;
    }

    public ImmutableList<Server> getServers() {
        return ImmutableList.copyOf(serverUtil.getServers());
    }

    public ImmutableList<Player> getOnlinePlayers() {
        return ImmutableList.copyOf(new ArrayList<>(players.values()));
    }

    public void addPlayer(Player player) {
        statUtil.newLogin();
        players.put(player.getUniqueId(), player);
        String server = removeRegisteringPlayer(player.getUniqueId());
        if (player.getServer().equalsIgnoreCase("unknown") && server != null) {
            Server s = Launcher.getDashboard().getServerUtil().getServer(server);
            if (s != null) s.changeCount(1);
            player.setServer(server);
        }
        if (player.isNewGuest()) {
            player.runTutorial();
        }
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

    public void logout(UUID uuid) {
        Player player = getPlayer(uuid);
        if (player != null) {
            if (!player.getServer().equalsIgnoreCase("unknown")) {
                Server s = getServerUtil().getServer(player.getServer());
                if (s != null) s.changeCount(-1);
            }
            if (player.getTutorial() != null) player.getTutorial().cancel();
            mongoHandler.logout(player);
            Launcher.getDashboard().getLogger().info("Player Quit: " + player.getUsername() + "|" + uuid.toString());
        } else {
            Launcher.getDashboard().getLogger().info("Player Quit: " + uuid.toString());
        }
        chatUtil.logout(uuid);
        PacketKick packet = new PacketKick("See ya real soon!");
        PacketContainer kick = new PacketContainer(uuid, packet.getJSON().toString());
        sendToAllConnections(kick, null, PacketConnectionType.ConnectionType.AUDIOSERVER);
        players.remove(uuid);
    }

    /**
     * Send a packet to connections based on several checks
     *
     * @param packet the packet to send
     * @param check  an optional callback that lets a custom verification be performed
     * @param types  an optional list of types to filter out by
     */
    public void sendToAllConnections(BasePacket packet, SocketCallback check, PacketConnectionType.ConnectionType... types) {
        List<PacketConnectionType.ConnectionType> matchingTypes = new ArrayList<>(Arrays.asList(types));
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if ((!matchingTypes.isEmpty() && !matchingTypes.contains(dash.getType()))
                    || (check != null && !check.verify(dash))) continue;
            dash.send(packet);
        }
    }

    /**
     * Send multiple packets to connections based on a check
     *
     * @param check   a callback that lets a custom verification be performed
     * @param packets the packets to send
     */
    public void sendToAllConnections(SocketCallback check, List<BasePacket> packets) {
        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!check.verify(dash)) continue;
            packets.forEach(dash::send);
        }
    }

    public List<String> getJoinServers() {
        return new ArrayList<>(joinServers);
    }

    public void setupShowReminder() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.of("America/New_York");
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNext10 = zonedNow.withHour(9).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext10_2 = zonedNow.withHour(9).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext10_3 = zonedNow.withHour(9).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext14 = zonedNow.withHour(13).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext14_2 = zonedNow.withHour(13).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext14_3 = zonedNow.withHour(13).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext18 = zonedNow.withHour(17).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext18_2 = zonedNow.withHour(17).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext18_3 = zonedNow.withHour(17).withMinute(40).withSecond(0);
        ZonedDateTime zonedNext22 = zonedNow.withHour(21).withMinute(20).withSecond(0);
        ZonedDateTime zonedNext22_2 = zonedNow.withHour(21).withMinute(30).withSecond(0);
        ZonedDateTime zonedNext22_3 = zonedNow.withHour(21).withMinute(40).withSecond(0);

        if (zonedNow.compareTo(zonedNext10) > 0) zonedNext10 = zonedNext10.plusDays(1);
        if (zonedNow.compareTo(zonedNext10_2) > 0) zonedNext10_2 = zonedNext10_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext10_3) > 0) zonedNext10_3 = zonedNext10_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext14) > 0) zonedNext14 = zonedNext14.plusDays(1);
        if (zonedNow.compareTo(zonedNext14_2) > 0) zonedNext14_2 = zonedNext14_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext14_3) > 0) zonedNext14_3 = zonedNext14_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext18) > 0) zonedNext18 = zonedNext18.plusDays(1);
        if (zonedNow.compareTo(zonedNext18_2) > 0) zonedNext18_2 = zonedNext18_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext18_3) > 0) zonedNext18_3 = zonedNext18_3.plusDays(1);
        if (zonedNow.compareTo(zonedNext22) > 0) zonedNext22 = zonedNext22.plusDays(1);
        if (zonedNow.compareTo(zonedNext22_2) > 0) zonedNext22_2 = zonedNext22_2.plusDays(1);
        if (zonedNow.compareTo(zonedNext22_3) > 0) zonedNext22_3 = zonedNext22_3.plusDays(1);

        long d1 = Duration.between(zonedNow, zonedNext10).getSeconds();
        long d2 = Duration.between(zonedNow, zonedNext10_2).getSeconds();
        long d3 = Duration.between(zonedNow, zonedNext10_3).getSeconds();
        long d4 = Duration.between(zonedNow, zonedNext14).getSeconds();
        long d5 = Duration.between(zonedNow, zonedNext14_2).getSeconds();
        long d6 = Duration.between(zonedNow, zonedNext14_3).getSeconds();
        long d7 = Duration.between(zonedNow, zonedNext18).getSeconds();
        long d8 = Duration.between(zonedNow, zonedNext18_2).getSeconds();
        long d9 = Duration.between(zonedNow, zonedNext18_3).getSeconds();
        long d10 = Duration.between(zonedNow, zonedNext22).getSeconds();
        long d11 = Duration.between(zonedNow, zonedNext22_2).getSeconds();
        long d12 = Duration.between(zonedNow, zonedNext22_3).getSeconds();

        ScheduledExecutorService sch = Executors.newScheduledThreadPool(1);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 40 minutes!"), d1,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 30 minutes!"), d2,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10am Show in 20 minutes!"), d3,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 40 minutes!"), d4,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 30 minutes!"), d5,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 2pm Show in 20 minutes!"), d6,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 40 minutes!"), d7,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 30 minutes!"), d8,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 6pm Show in 20 minutes!"), d9,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 40 minutes!"), d10,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 30 minutes!"), d11,
                24 * 60 * 60, TimeUnit.SECONDS);
        sch.scheduleAtFixedRate(new ShowReminder(ChatColor.GREEN + "Please get ready to run the 10pm Show in 20 minutes!"), d12,
                24 * 60 * 60, TimeUnit.SECONDS);
    }

    public String getRandomToken() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char c = chars[getRandom().nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    public String getServerIconBase64() {
        String encodedFile = "";
        File serverIcon = new File("server-icon.png");
        if (!serverIcon.exists()) {
            return encodedFile;
        }
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(serverIcon);
            byte[] bytes = new byte[(int) serverIcon.length()];
            fileInputStreamReader.read(bytes);
            encodedFile = new String(Base64.getEncoder().encode(bytes), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error loading server icon", e);
        }
        return encodedFile;
    }

    public boolean hasPlayer(UUID uuid) {
        return registering.containsKey(uuid);
    }

    public void addRegisteringPlayer(UUID uuid) {
        registering.put(uuid, "");
    }

    public void setRegisteringPlayerServer(UUID uuid, String server) {
        registering.put(uuid, server);
    }

    public String removeRegisteringPlayer(UUID uuid) {
        return registering.remove(uuid);
    }
}
