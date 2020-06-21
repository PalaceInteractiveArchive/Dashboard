package network.palace.dashboard.handlers;

import network.palace.dashboard.server.DashboardSocketChannel;

public interface SocketCallback {

    boolean verify(DashboardSocketChannel channel);
}
