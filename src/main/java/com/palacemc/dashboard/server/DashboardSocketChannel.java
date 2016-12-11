package com.palacemc.dashboard.server;

import com.palacemc.dashboard.packets.BasePacket;
import com.palacemc.dashboard.packets.dashboard.PacketConnectionType;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.Setter;

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
    @Getter protected long id = nextId.getAndIncrement();
    @Getter private long connectTime = System.currentTimeMillis();

    @Getter @Setter private PacketConnectionType.ConnectionType type = PacketConnectionType.ConnectionType.UNKNOWN;
    @Getter private UUID bungeeID = UUID.randomUUID();
    @Getter @Setter private String serverName = "";

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

    public void send(String message) {
        writeAndFlush(new TextWebSocketFrame(message));
    }

    public void send(BasePacket packet) {
        send(packet.getJSON().toString());
    }
}