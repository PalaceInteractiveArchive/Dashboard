package network.palace.dashboard.utils;

import com.mongodb.client.model.Filters;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.ChatColor;
import network.palace.dashboard.handlers.CurrencyType;
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
import org.bson.Document;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.Key;
import java.security.KeyPair;
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
        Dashboard dashboard = Launcher.getDashboard();
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
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        serverChannel = future.channel();
                        dashboard.getLogger().info("Votifier enabled on socket " + serverChannel.localAddress() + ".");
                    } else {
                        SocketAddress socketAddress = future.channel().localAddress();
                        if (socketAddress == null) {
                            socketAddress = new InetSocketAddress(host, port);
                        }
                        dashboard.getLogger().info("Votifier was not able to bind to " + socketAddress);
                    }
                });
    }

    public void stop() {
        serverGroup.shutdownGracefully();
    }

    public void onVoteReceived(Channel channel, final Vote vote, VotifierSession.ProtocolVersion protocolVersion) throws Exception {
        Dashboard dashboard = Launcher.getDashboard();
        if (debug) {
            if (protocolVersion == VotifierSession.ProtocolVersion.ONE) {
                dashboard.getLogger().info("Got a protocol v1 vote record from " + channel.remoteAddress() + " -> " + vote);
            } else {
                dashboard.getLogger().info("Got a protocol v2 vote record from " + channel.remoteAddress() + " -> " + vote);
            }
        }
        String username = vote.getUsername();
        Player player = dashboard.getPlayer(username);
        final UUID uuid;
        if (player == null) {
            UUID temp = dashboard.getMongoHandler().usernameToUUID(username);
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
        dashboard.getSchedulerManager().runAsync(() -> vote(uuid, finalId));
    }

    public void onError(Channel channel, Throwable throwable) {
        Dashboard dashboard = Launcher.getDashboard();
//        if (debug) {
//            dashboard.getLogger().error("Unable to process vote from " + channel.remoteAddress());
//        } else {
        dashboard.getLogger().error("Unable to process vote from " + channel.remoteAddress());
//        }
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
        Dashboard dashboard = Launcher.getDashboard();
        try {
            long lastVote;
            Document voteDoc = dashboard.getMongoHandler().getPlayer(uuid, new Document("vote", 1));
            if (voteDoc == null || !voteDoc.containsKey("vote.lastTime")) {
                lastVote = 0;
            } else {
                lastVote = voteDoc.getLong("vote.lastTime");
            }
            boolean cancel = false;
            if (System.currentTimeMillis() - lastVote <= 21600000) {
                cancel = true;
            }
            Player p = dashboard.getPlayer(uuid);
            if (cancel) {
                if (p != null) {
                    p.sendMessage(ChatColor.RED + "You already claimed a reward for voting in the past 6 hours!");
                }
                return;
            }
            Document update = new Document("$inc", new Document("tokens", 5)).append("$set", new Document("vote", new Document("lastTime",
                    System.currentTimeMillis()).append("lastSite", serverId)));
            dashboard.getMongoHandler().getPlayerCollection().updateOne(Filters.eq("uuid", uuid.toString()), update);
            dashboard.getMongoHandler().logTransaction(uuid, 5, "Vote", CurrencyType.TOKENS, false);
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
                    if (dashboard.getServer(dash.getServerName()).getUniqueId().equals(dashboard.getServer(p.getServer()).getUniqueId())) {
                        dash.send(packet);
                        return;
                    }
                } catch (Exception e) {
                    Launcher.getDashboard().getLogger().error("Error processing vote", e);
                }
            }
        } catch (Exception e) {
            Launcher.getDashboard().getLogger().error("Error processing vote", e);
        }
    }

    public void reload() {
        voteServices.clear();
        for (Document doc : Launcher.getDashboard().getMongoHandler().getVotingCollection().find()) {
            voteServices.put(doc.getString("name"), doc.getInteger("siteid"));
        }
    }
}
