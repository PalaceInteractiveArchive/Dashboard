package network.palace.dashboard.server;

import io.netty.channel.socket.nio.NioServerSocketChannel;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;

import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by Marc on 6/15/15
 */
public class DashboardServerSocketChannel extends NioServerSocketChannel {

    protected int doReadMessages(List<Object> buf)
            throws Exception {
        SocketChannel ch = javaChannel().accept();
        try {
            if (ch != null) {
                buf.add(new DashboardSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            Dashboard dashboard = Launcher.getDashboard();
            dashboard.getLogger().error("Failed to create a new channel from an accepted socket", t);
            try {
                ch.close();
            } catch (Throwable t2) {
                dashboard.getLogger().error("Failed to close a socket", t2);
            }
        }

        return 0;
    }
}