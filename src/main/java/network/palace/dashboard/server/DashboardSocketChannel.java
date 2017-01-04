package network.palace.dashboard.server;

import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Marc on 7/14/16
 */
@SuppressWarnings("unchecked")
public class DashboardSocketChannel extends NioSocketChannel {
    private static AtomicLong nextId = new AtomicLong(0L);
    protected long id = nextId.getAndIncrement();
    private PacketConnectionType.ConnectionType type = PacketConnectionType.ConnectionType.UNKNOWN;
    private UUID bungeeID = UUID.randomUUID();
    private String serverName = "";
    private long connectTime = System.currentTimeMillis();

    public DashboardSocketChannel(PacketConnectionType.ConnectionType type) {
        this.type = type;
    }

    public DashboardSocketChannel(SelectorProvider provider) {
        super(provider);
    }

    public DashboardSocketChannel(SocketChannel socket) {
        super(socket);
    }

    public DashboardSocketChannel(Channel parent, SocketChannel socket) {
        super(parent, socket);
    }

    public long getId() {
        return this.id;
    }

    public void send(String message) {
        writeAndFlush(new TextWebSocketFrame(message));
    }

    public void setType(PacketConnectionType.ConnectionType type) {
        this.type = type;
    }

    public PacketConnectionType.ConnectionType getType() {
        return type;
    }

    public UUID getBungeeID() {
        return bungeeID;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void send(BasePacket packet) {
        send(packet.getJSON().toString());
    }

    public long getConnectTime() {
        return connectTime;
    }
}