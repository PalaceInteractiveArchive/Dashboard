package com.palacemc.dashboard;

import com.palacemc.dashboard.scheduler.SchedulerManager;
import com.palacemc.dashboard.server.DashboardServerSocketChannel;
import com.palacemc.dashboard.server.WebSocketServerInitializer;
import com.palacemc.dashboard.slack.SlackAttachment;
import com.palacemc.dashboard.slack.SlackMessage;
import com.palacemc.dashboard.utils.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Innectic
 * @since 12/9/16
 */
public class Launcher {

    @Getter private static Dashboard dashboard;

    public static void main(String[] args) {
        dashboard = new Dashboard();

        PatternLayout layout = new PatternLayout("[%d{HH:mm:ss}] [%p] - %m%n");
        dashboard.getLogger().addAppender(new ConsoleAppender(layout));

        try {
            dashboard.getLogger().addAppender(new FileAppender(layout, "dashboard.log", true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        dashboard.getLogger().info("Starting up Dashboard...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dashboard.setStartTime(System.currentTimeMillis());
            dashboard.getLogger().warn("Shutting down Dashboard...");
            dashboard.getSlackUtil().sendDashboardMessage(new SlackMessage(),
                    Arrays.asList(new SlackAttachment("Dashboard went offline! #devs").color("danger")));
        }));

        dashboard.setSchedulerManager(new SchedulerManager());

        try {
            dashboard.setSqlUtil(new SqlUtil());
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        dashboard.loadConfiguration();
        dashboard.loadMOTD();
        dashboard.loadServerTypes();
        dashboard.loadJoinServers();

        if (dashboard.isTestNetwork()) {
            dashboard.getLogger().info("Test network detected, disabling statistics collection!");
        }

        try {
            dashboard.setServerUtil(new ServerUtil());
        } catch (IOException e) {
            e.printStackTrace();
        }

        dashboard.setChatUtil(new ChatUtil());
        dashboard.setCommandUtil(new CommandUtil());
        dashboard.setPartyUtil(new PartyUtil());
        dashboard.setSlackUtil(new SlackUtil());
        dashboard.setWarningUtil(new WarningUtil());

        try {
            dashboard.setSiteUtil(new SiteUtil());
        } catch (Exception e) {
            e.printStackTrace();
        }

        dashboard.setAfkUtil(new AFKUtil());
        dashboard.setStatUtil(new StatUtil());

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup).channel(DashboardServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer());
            Channel channel = serverBootstrap.bind(dashboard.getPORT()).sync().channel();

            dashboard.getLogger().info("Dashboard started on port " + dashboard.getPORT());

            dashboard.getSlackUtil().sendDashboardMessage(new SlackMessage(),
                    Arrays.asList(new SlackAttachment("Dashboard has successfully started up!").color("good")));
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
