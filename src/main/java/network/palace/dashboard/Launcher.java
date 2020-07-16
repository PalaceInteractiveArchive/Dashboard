package network.palace.dashboard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.forums.Forum;
import network.palace.dashboard.mongo.MongoHandler;
import network.palace.dashboard.packets.audio.PacketHeartbeat;
import network.palace.dashboard.scheduler.SchedulerManager;
import network.palace.dashboard.server.DashboardServerSocketChannel;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;
import network.palace.dashboard.server.WebSocketServerInitializer;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;
import network.palace.dashboard.utils.*;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * @author Innectic
 * @since 4/29/2017
 */
public class Launcher {

    @Getter private static Dashboard dashboard;
//    private ConsoleReader reader;
//    @Getter private static Logger packetLogger;

    public Launcher() {
        System.out.println("Launching Dashboard with " + getClass().getClassLoader().getClass().getSimpleName());
        dashboard = new Dashboard();

//        /* Configure Logging */
//        try {
//            // Have ConsoleReader listen to System.in and System.out
//            reader = new ConsoleReader(System.in, System.out);
//            reader.setExpandEvents(false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Pass to logs instead of directly to standard output
//        java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
//        global.setUseParentHandlers(false);
//        for (Handler handler : global.getHandlers()) {
//            global.removeHandler(handler);
//        }
//        global.addHandler(new ForwardLogHandler());
//
//        // Output this.reader to System.out
//        new TerminalConsoleWriterThread(System.out, this.reader).start();
//
//        // Configure RootLogger to output as System.out and System.err
//        Logger logger = (Logger) LogManager.getRootLogger();
//        for (Appender appender : logger.getAppenders().values()) {
//            if (appender instanceof ConsoleAppender) {
//                logger.removeAppender(appender);
//            }
//        }
//        System.setOut(new PrintStream(new LoggerOutputStream(logger, Level.INFO), true));
//        System.setErr(new PrintStream(new LoggerOutputStream(logger, Level.WARN), true));
//
//        // Create PacketLogger only for logging incoming/outgoing packets
//        packetLogger = LogManager.getLogger("PacketOut");

        java.util.logging.Logger mongoLogger = java.util.logging.Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(java.util.logging.Level.OFF);
        /* Finished Configuring Logging */

        dashboard.setStartTime(System.currentTimeMillis());
//        packetLogger.info("Starting up Dashboard at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date.from(Instant.now())));
        dashboard.getLogger().info("Starting up Dashboard at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date.from(Instant.now())));
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        dashboard.loadConfiguration();
        try {
            dashboard.getLogger().info("Initializing MongoDB Handler...");
            dashboard.setMongoHandler(new MongoHandler());
            dashboard.getLogger().info("Finished initializing MongoDB Handler!");
            dashboard.loadMaintenanceSettings();
//            dashboard.setSqlUtil(new SqlUtil());
            dashboard.setStatUtil(new StatUtil());
        } catch (Exception e) {
            Launcher.getDashboard().getLogger().error("Error starting Dashboard!", e);
            System.exit(0);
        }
        dashboard.setRandom(new Random());
        dashboard.setSchedulerManager(new SchedulerManager());
        dashboard.setInventoryUtil(new InventoryUtil());
        dashboard.setModerationUtil(new ModerationUtil());

        dashboard.loadMOTD();
        dashboard.loadJoinServers();

        if (!dashboard.isTestNetwork()) dashboard.setSocketConnection(new SocketConnection());

        dashboard.setServerUtil(new ServerUtil());
        dashboard.setChatUtil(new ChatUtil());
        dashboard.setCommandUtil(new CommandUtil());
        dashboard.setEmojiUtil(new EmojiUtil());
        dashboard.setPartyUtil(new PartyUtil());
        dashboard.setSlackUtil(new SlackUtil());
        dashboard.setWarningUtil(new WarningUtil());
        dashboard.setPasswordUtil(new PasswordUtil());
        dashboard.setFriendUtil(new FriendUtil());
        dashboard.setStrictThreshold(0.5);
        dashboard.setStrictMode(false);
        try {
            dashboard.setSiteUtil(new SiteUtil());
        } catch (Exception e) {
            Launcher.getDashboard().getLogger().error("Error loading SiteUtil", e);
        }
        dashboard.setAfkUtil(new AFKUtil());
        dashboard.setVoteUtil(new VoteUtil());
        dashboard.setShowUtil(new ShowUtil());
        dashboard.setGuideUtil(new GuideUtil());
        try {
            dashboard.setForum(new Forum());
        } catch (Exception e) {
            Launcher.getDashboard().getLogger().error("Error loading Forum", e);
        }
        dashboard.setupShowReminder();
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
            Channel ch = b.bind(new InetSocketAddress(dashboard.HOST, dashboard.PORT)).sync().channel();
            dashboard.getLogger().info("Dashboard started at " + dashboard.HOST + ":" + dashboard.PORT);
            dashboard.getSlackUtil().sendDashboardMessage(new SlackMessage(),
                    Collections.singletonList(new SlackAttachment("Dashboard has successfully started up!").color("good")));
            ch.closeFuture().sync();
        } catch (Exception e) {
            Launcher.getDashboard().getLogger().error("Error with web socket server", e);
        } finally {
            dashboard.getMongoHandler().disconnect();

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
