package com.palacemc.dashboard.server;

import com.palacemc.dashboard.Launcher;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by Marc on 6/15/15
 */
public class DashboardServerSocketChannel extends NioServerSocketChannel {

    protected int doReadMessages(List<Object> buf) {
        SocketChannel ch = null;

        try {
            ch = javaChannel().accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (ch != null) {
                buf.add(new DashboardSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            Launcher.getDashboard().getLogger().error("Failed to create a new channel from an accepted socket.");
            t.printStackTrace();

            try {
                ch.close();
            } catch (Throwable t2) {
                Launcher.getDashboard().getLogger().error("Failed to close a socket.");
                t2.printStackTrace();
            }
        }

        return 0;
    }
}