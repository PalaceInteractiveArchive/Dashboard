package network.palace.dashboard.utils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.handlers.ChatColor;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.packets.dashboard.PacketUpdateEconomy;
import network.palace.dashboard.server.DashboardSocketChannel;
import network.palace.dashboard.server.WebSocketServerHandler;
import network.palace.dashboard.vote.Vote;
import network.palace.dashboard.vote.VotifierGreetingHandler;
import network.palace.dashboard.vote.VotifierProtocolDifferentiator;
import network.palace.dashboard.vote.VotifierSession;
import network.palace.dashboard.vote.protocol.VoteInboundHandler;
import network.palace.dashboard.vote.protocol.rsa.RSAIO;
import network.palace.dashboard.vote.protocol.rsa.RSAKeygen;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.Key;
import java.security.KeyPair;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Marc on 1/15/17.
 */
public class VoteUtil {
    private HashMap<String, Integer> voteServices = new HashMap<>();
    private Channel serverChannel;
    private NioEventLoopGroup serverGroup;
    public static AttributeKey<VoteUtil> KEY = AttributeKey.valueOf("votifier_plugin");
    private KeyPair keyPair;
    private boolean debug = true;
    private Map<String, Key> tokens = new HashMap<>();
    private String host = "0.0.0.0";
    private int port = 8192;

    public VoteUtil() {
        File rsaDirectory = new File("vote/");
        try {
            if (!rsaDirectory.exists()) {
                rsaDirectory.mkdir();
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsaDirectory, keyPair);
            } else {
                keyPair = RSAIO.load(rsaDirectory);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error reading RSA tokens", ex);
        }
        serverGroup = new NioEventLoopGroup(2);
        new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(serverGroup)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        channel.attr(VotifierSession.KEY).set(new VotifierSession());
                        channel.attr(KEY).set(VoteUtil.this);
                        channel.pipeline().addLast("greetingHandler", new VotifierGreetingHandler());
                        channel.pipeline().addLast("protocolDifferentiator", new VotifierProtocolDifferentiator(false, true));
                        channel.pipeline().addLast("voteHandler", new VoteInboundHandler(VoteUtil.this));
                    }
                })
                .bind(host, port)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            serverChannel = future.channel();
                            Dashboard.getLogger().info("Votifier enabled on socket " + serverChannel.localAddress() + ".");
                        } else {
                            SocketAddress socketAddress = future.channel().localAddress();
                            if (socketAddress == null) {
                                socketAddress = new InetSocketAddress(host, port);
                            }
                            Dashboard.getLogger().error("Votifier was not able to bind to " + socketAddress, future.cause());
                        }
                    }
                });
    }

    public void stop() {
        serverGroup.shutdownGracefully();
    }

    public void onVoteReceived(Channel channel, final Vote vote, VotifierSession.ProtocolVersion protocolVersion) throws Exception {
        if (debug) {
            if (protocolVersion == VotifierSession.ProtocolVersion.ONE) {
                Dashboard.getLogger().info("Got a protocol v1 vote record from " + channel.remoteAddress() + " -> " + vote);
            } else {
                Dashboard.getLogger().info("Got a protocol v2 vote record from " + channel.remoteAddress() + " -> " + vote);
            }
        }
        String username = vote.getUsername();
        Player player = Dashboard.getPlayer(username);
        final UUID uuid;
        if (player == null) {
            UUID temp = Dashboard.sqlUtil.uuidFromUsername(username);
            if (temp == null) {
                return;
            } else {
                uuid = temp;
            }
        } else {
            uuid = player.getUniqueId();
            player.sendMessage(ChatColor.GREEN + "Thanks for supporting our server! You received " + ChatColor.GOLD
                    + "" + ChatColor.BOLD + "5 Tokens" + ChatColor.RESET + ChatColor.GREEN + " for voting for us!");
        }
        int id = 0;
        String service = vote.getServiceName().toLowerCase();
        for (Map.Entry<String, Integer> entry : new HashSet<>(voteServices.entrySet())) {
            if (entry.getKey().trim().equalsIgnoreCase(service.trim())) {
                id = entry.getValue();
                break;
            }
        }
        final int finalId = id;
        Dashboard.schedulerManager.runAsync(new Runnable() {
            @Override
            public void run() {
                vote(uuid, finalId);
            }
        });
    }

    public void onError(Channel channel, Throwable throwable) {
        if (debug) {
            Dashboard.getLogger().error("Unable to process vote from " + channel.remoteAddress(), throwable);
        } else {
            Dashboard.getLogger().error("Unable to process vote from " + channel.remoteAddress());
        }
    }

    public Map<String, Key> getTokens() {
        return tokens;
    }

    public KeyPair getProtocolV1Key() {
        return keyPair;
    }

    public String getVersion() {
        return "v1";
    }

    public boolean isDebug() {
        return debug;
    }

    private void vote(UUID uuid, int serverId) {
        try (Connection connection = Dashboard.sqlUtil.getConnection()) {
            PreparedStatement q = connection.prepareStatement("SELECT vote FROM player_data WHERE uuid=?");
            q.setString(1, uuid.toString());
            ResultSet qres = q.executeQuery();
            if (!qres.next()) {
                return;
            }
            boolean cancel = false;
            if (System.currentTimeMillis() - qres.getLong("vote") <= 21600000) {
                cancel = true;
            }
            qres.close();
            q.close();
            Player p = Dashboard.getPlayer(uuid);
            if (cancel) {
                if (p != null) {
                    p.sendMessage(ChatColor.RED + "You already claimed a reward for voting in the past 6 hours!");
                }
                return;
            }
            PreparedStatement sql = connection.prepareStatement("UPDATE player_data SET tokens=tokens+5,vote=?," +
                    "lastvote=? WHERE uuid=?");
            sql.setLong(1, System.currentTimeMillis());
            sql.setInt(2, serverId);
            sql.setString(3, uuid.toString());
            sql.execute();
            sql.close();
            PreparedStatement log = connection.prepareStatement("INSERT INTO economy_logs (uuid, amount, type, source," +
                    " server, timestamp) VALUES ('" + uuid.toString() + "', '5', 'add tokens', 'Vote', " +
                    "'Dashboard', '" + System.currentTimeMillis() / 1000L + "')");
            log.execute();
            log.close();
            if (p == null) {
                return;
            }
            PacketUpdateEconomy packet = new PacketUpdateEconomy(p.getUniqueId());
            for (Object o : WebSocketServerHandler.getGroup()) {
                DashboardSocketChannel dash = (DashboardSocketChannel) o;
                if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                    continue;
                }
                try {
                    if (Dashboard.getServer(dash.getServerName()).getUniqueId().equals(Dashboard.getServer(p.getServer()).getUniqueId())) {
                        dash.send(packet);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        voteServices.clear();
        try (Connection connection = Dashboard.activityUtil.getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT siteid,name FROM vote");
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                voteServices.put(result.getString("name").toLowerCase(), result.getInt("siteid"));
            }
            result.close();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
