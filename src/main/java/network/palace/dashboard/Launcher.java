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
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Innectic
 * @since 4/29/2017
 */
public class Launcher {

    @Getter private static Dashboard dashboard;

    public Launcher() {
        dashboard = new Dashboard();

        dashboard.setStartTime(System.currentTimeMillis());
        PatternLayout layout = new PatternLayout("[%d{HH:mm:ss}] [%p] - %m%n");
        dashboard.getLogger().addAppender(new ConsoleAppender(layout));
        try {
            dashboard.getLogger().addAppender(new FileAppender(layout, "dashboard.log", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dashboard.getErrors().addAppender(new FileAppender(layout, "error.log", true));
        } catch (IOException e) {
            ErrorUtil.logError("Error opening error.log", e);
        }
        try {
            dashboard.getPlayerLog().addAppender(new FileAppender(layout, "players.log", true));
        } catch (IOException e) {
            ErrorUtil.logError("Error opening players.log", e);
        }
        dashboard.getLogger().info("Starting up Dashboard...");
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        dashboard.loadConfiguration();
        try {
            dashboard.getLogger().info("Initializing MongoDB Handler...");
            dashboard.setMongoHandler(new MongoHandler());
            dashboard.getLogger().info("Finished initializing MongoDB Handler!");
            dashboard.getLogger().info("Initializing SQLUtil...");
            dashboard.setSqlUtil(new SqlUtil());
            dashboard.getLogger().info("Finished initializing SQLUtil!");
            dashboard.getLogger().info("Initializing StatUtil...");
            dashboard.setStatUtil(new StatUtil());
            dashboard.getLogger().info("Finished initializing StatUtil!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        if (dashboard.isMaintenance()) dashboard.loadMaintenanceSettings();
        dashboard.setRandom(new Random());
        dashboard.getLogger().info("Initializing ScheduleManager...");
        dashboard.setSchedulerManager(new SchedulerManager());
        dashboard.getLogger().info("Finished initializing ScheduleManager!");
        dashboard.getLogger().info("Initializing InventoryUtil...");
        dashboard.setInventoryUtil(new InventoryUtil());
        dashboard.getLogger().info("Finished initializing InventoryUtil!");
        dashboard.getLogger().info("Initializing ModerationUtil...");
        dashboard.setModerationUtil(new ModerationUtil());
        dashboard.getLogger().info("Finished initializing ModerationUtil!");

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
            e.printStackTrace();
        }
        dashboard.setAfkUtil(new AFKUtil());
        dashboard.setVoteUtil(new VoteUtil());
        dashboard.setShowUtil(new ShowUtil());
        dashboard.setGuideUtil(new GuideUtil());
        try {
            dashboard.setForum(new Forum());
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } finally {
            dashboard.getMongoHandler().disconnect();

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
