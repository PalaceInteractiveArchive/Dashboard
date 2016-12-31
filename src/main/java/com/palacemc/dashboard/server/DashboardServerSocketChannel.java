package com.palacemc.dashboard.server;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by Marc on 6/15/15
 */
public class DashboardServerSocketChannel extends NioServerSocketChannel {

    private Dashboard dashboard = Launcher.getDashboard();

    protected int doReadMessages(List<Object> buffer) {
        SocketChannel socketChannel = null;

        try {
            socketChannel = javaChannel().accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (socketChannel != null) {
                buffer.add(new DashboardSocketChannel(this, socketChannel));
                return 1;
            }
        } catch (Throwable t) {
            dashboard.getLogger().error("Failed to create a new channel from an accepted socket.", t);

            try {
                socketChannel.close();
            } catch (Throwable t2) {
                dashboard.getLogger().error("Failed to close a socket.", t2);
            }
        }

        return 0;
    }
}