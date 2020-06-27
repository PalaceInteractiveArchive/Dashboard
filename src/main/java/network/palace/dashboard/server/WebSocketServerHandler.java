package network.palace.dashboard.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.chat.*;
import network.palace.dashboard.discordSocket.DiscordCacheInfo;
import network.palace.dashboard.discordSocket.SocketConnection;
import network.palace.dashboard.handlers.*;
import network.palace.dashboard.packets.BasePacket;
import network.palace.dashboard.packets.arcade.GameState;
import network.palace.dashboard.packets.arcade.PacketGameStatus;
import network.palace.dashboard.packets.audio.PacketContainer;
import network.palace.dashboard.packets.audio.PacketGetPlayer;
import network.palace.dashboard.packets.audio.PacketPlayerInfo;
import network.palace.dashboard.packets.bungee.PacketBungeeID;
import network.palace.dashboard.packets.bungee.PacketPlayerListInfo;
import network.palace.dashboard.packets.bungee.PacketServerIcon;
import network.palace.dashboard.packets.dashboard.*;
import network.palace.dashboard.packets.inventory.PacketInventoryContent;
import network.palace.dashboard.packets.inventory.Resort;
import network.palace.dashboard.packets.park.*;
import network.palace.dashboard.packets.park.queue.*;
import network.palace.dashboard.slack.SlackAttachment;
import network.palace.dashboard.slack.SlackMessage;
import network.palace.dashboard.utils.DateUtil;
import network.palace.dashboard.utils.IPUtil;
import org.apache.logging.log4j.Level;
import org.influxdb.dto.Point;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Marc
 * @since 6/15/15
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private WebSocketServerHandshaker handshaker;

    public static final String MINIGAME_REGEX = "mini-(\\w+)([1-9])";

    private final String MINIGAME_SERVER_NAME = "Arcade";

    public static ChannelGroup getGroup() {
        return channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channels.add(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(null, null, true, (int) Math.pow(2, 20));
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        try {
            Dashboard dashboard = Launcher.getDashboard();

            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            if (frame instanceof PongWebSocketFrame) {
                return;
            }
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String.format("%s frame types not supported",
                        frame.getClass().getName()));
            }
            String request = ((TextWebSocketFrame) frame).text();
            JsonObject object;
            try {
                object = (JsonObject) new JsonParser().parse(request);
            } catch (Exception e) {
                dashboard.getLogger().warning("Error processing packet [" + request + "] from " +
                        ((io.netty.channel.socket.SocketChannel) ctx).localAddress());
                return;
            }
            if (!object.has("id")) {
                return;
            }
            int id = object.get("id").getAsInt();
            if (id != 43) Launcher.getPacketLogger().log(Level.DEBUG, object.toString());
            dashboard.getStatUtil().packet();
            DashboardSocketChannel channel = (DashboardSocketChannel) ctx.channel();
            switch (id) {
                /*
                 * GetPlayer (Audio)
                 */
                case 13: {
                    PacketGetPlayer packet = new PacketGetPlayer().fromJSON(object);
                    String token = packet.getToken();
                    Player player = null;
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getAudioToken().equals(token)) {
                            player = tp;
                        }
                    }
                    PacketPlayerInfo info;
                    if (player == null || player.getAudioToken().equals("")) {
                        info = new PacketPlayerInfo(null, "", token, "");
                    } else {
                        info = new PacketPlayerInfo(player.getUniqueId(), token, player.getAudioToken(), player.getServer());
                        player.resetAudioToken();
                        try {
                            PacketAudioConnect connect = new PacketAudioConnect(player.getUniqueId());
                            DashboardSocketChannel socketChannel = Dashboard.getInstance(player.getServer());
                            if (socketChannel == null) return;
                            socketChannel.send(connect);
                        } catch (Exception ignored) {
                        }
                    }
                    channel.send(info);
                    break;
                }
                /*
                 * AudioServer Packet (Container)
                 */
                case 17: {
                    PacketContainer packet = new PacketContainer().fromJSON(object);
                    for (Object o : WebSocketServerHandler.getGroup()) {
                        DashboardSocketChannel dash = (DashboardSocketChannel) o;
                        if (!dash.getType().equals(PacketConnectionType.ConnectionType.AUDIOSERVER)) {
                            continue;
                        }
                        dash.send(packet);
                    }
                    break;
                }
                /*
                 * ConnectionType
                 */
                case 22: {
                    PacketConnectionType packet = new PacketConnectionType().fromJSON(object);
                    PacketConnectionType.ConnectionType type = packet.getType();
                    channel.setType(type);
                    switch (type) {
                        case BUNGEECORD: {
                            break;
                        }
                        case DAEMON: {
                            dashboard.getModerationUtil().sendMessage(ChatColor.GREEN + "A new daemon has connected to Dashboard.");
                            SlackMessage m = new SlackMessage("");
                            SlackAttachment a = new SlackAttachment("A new daemon has connected to dashboard from the IP Address " +
                                    channel.remoteAddress().getAddress().toString());
                            a.color("good");
                            dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a));
                            break;
                        }
                        case WEBCLIENT: {
                            break;
                        }
                        case INSTANCE: {
                            break;
                        }
                        case AUDIOSERVER: {
                            dashboard.getModerationUtil().sendMessage(ChatColor.GREEN +
                                    "The Audio Server has connected to Dashboard.");
                            SlackMessage m = new SlackMessage("");
                            SlackAttachment a = new SlackAttachment("The Audio Server has connected to Dashboard from the IP Address " +
                                    channel.remoteAddress().getAddress().toString());
                            a.color("good");
                            dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a));
                            break;
                        }
                    }
                    dashboard.getLogger().info("New " + type.name().toLowerCase() + " connection");
                    if (type.equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                        PacketUpdateMOTD motd = new PacketUpdateMOTD(dashboard.getMotd(), dashboard.getMotdMaintenance(),
                                dashboard.getInfo());
                        PacketOnlineCount count = new PacketOnlineCount(dashboard.getOnlinePlayers().size());
                        List<String> servers = new ArrayList<>();
                        for (Server s : dashboard.getServers()) {
                            servers.add(s.getName() + ";" + s.getAddress());
                        }
                        PacketServerList server = new PacketServerList(servers);
                        PacketTargetLobby lobby = new PacketTargetLobby(dashboard.getTargetServer());
                        PacketCommandList commands = dashboard.getCommandUtil().getTabCompleteCommandPacket();
                        PacketMaintenance maintenance = new PacketMaintenance(dashboard.isMaintenance());
                        PacketBungeeID bungeeID = new PacketBungeeID(channel.getBungeeID());
                        String base64 = Launcher.getDashboard().getServerIconBase64();
                        PacketServerIcon serverIcon = new PacketServerIcon(base64);
                        channel.send(motd);
                        channel.send(count);
                        channel.send(server);
                        channel.send(lobby);
                        channel.send(commands);
                        channel.send(maintenance);
                        channel.send(bungeeID);
                        channel.send(serverIcon);
                        if (dashboard.isMaintenance()) {
                            PacketMaintenanceWhitelist whitelist = new PacketMaintenanceWhitelist(dashboard.getMaintenanceWhitelist());
                            channel.send(whitelist);
                        }
                    }
                    if (type.equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                        PacketOnlineCount count = new PacketOnlineCount(dashboard.getOnlinePlayers().size());
                        channel.send(count);
                    }
                    break;
                }
                /*
                 * PlayerJoin
                 */
                case 23: {
                    PacketPlayerJoin packet = new PacketPlayerJoin().fromJSON(object);
                    dashboard.addRegisteringPlayer(packet.getUniqueId());
                    Player player = new Player(packet.getUniqueId(), packet.getUsername(), packet.getAddress(),
                            packet.getServer(), channel.getBungeeID(), packet.getMcVersion());
                    if (dashboard.getPlayer(player.getUniqueId()) != null) {
                        player.kickPlayer("You are already connected to The Palace Network!");
                        return;
                    }
                    dashboard.getMongoHandler().login(player);
                    dashboard.getSchedulerManager().runAsync(() -> {
                        IPUtil.ProviderData data = IPUtil.getProviderData(packet.getAddress());
                        if (data != null) {
                            player.setIsp(data.getIsp());
                            if (!player.getIsp().isEmpty()) {
                                ProviderBan ban = dashboard.getMongoHandler().getProviderBan(player.getIsp());
                                if (ban != null) {
                                    player.kickPlayer(ChatColor.RED + "Your ISP (Internet Service Provider) Has Been Blocked From Our Network");
                                }
                            }
                            dashboard.getMongoHandler().updateProviderData(player.getUniqueId(), data);
                        }
                    });

                    String address = player.getAddress();

                    int count = 1;

                    for (Player p : dashboard.getOnlinePlayers()) {
                        if (p.getRank().getRankId() >= Rank.CHARACTER.getRankId()) {
                            count = 0;
                            break;
                        }
                        if (!p.getUniqueId().equals(player.getUniqueId()) && p.getAddress().equalsIgnoreCase(address)) {
                            count++;
                        }
                    }

                    if (count > 5) {
                        SpamIPWhitelist whitelist = dashboard.getMongoHandler().getSpamIPWhitelist(address);
                        int limit = whitelist == null ? 5 : whitelist.getLimit();
                        if (count > limit) {
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    dashboard.getModerationUtil().announceSpamIPConnect(limit, address);
                                    player.kickPlayer(ChatColor.RED + "There are already " + ChatColor.GREEN + "" +
                                            ChatColor.BOLD + limit + " " + ChatColor.RED + "accounts connected from this IP Address.\n" +
                                            ChatColor.RED + "If you need more than " + ChatColor.GREEN + "" +
                                            ChatColor.BOLD + limit + " " + ChatColor.RED + "accounts online at a time, email us at " +
                                            ChatColor.AQUA + "support@thepalacemc.com.");
                                }
                            }, 2000L);
                        }
                    }
                    break;
                }
                /*
                 * PlayerDisconnect
                 */
                case 24: {
                    PacketPlayerDisconnect packet = new PacketPlayerDisconnect().fromJSON(object);
                    dashboard.logout(packet.getUniqueId());
                    break;
                }
                /*
                 * PlayerChat
                 */
                case 25: {
                    PacketPlayerChat packet = new PacketPlayerChat().fromJSON(object);
                    dashboard.getChatUtil().chatEvent(packet);
                    break;
                }
                /*
                 * Message
                 */
                case 26: {
                    PacketMessage packet = new PacketMessage().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    String msg = packet.getMessage();
                    Player tp = dashboard.getPlayer(uuid);
                    if (tp == null) {
                        return;
                    }
                    tp.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                    break;
                }
                /*
                 * Server Switch
                 */
                case 27: {
                    PacketServerSwitch packet = new PacketServerSwitch().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    String target = packet.getTarget();
                    dashboard.getServerUtil().serverSwitchEvent(uuid, target);
                    break;
                }
                /*
                 * Send To Server
                 */
                case 32: {
                    PacketSendToServer packet = new PacketSendToServer().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    String server = packet.getServer();
                    Player tp = dashboard.getPlayer(uuid);
                    if (tp == null) {
                        return;
                    }
                    Server target = dashboard.getServer(server);
                    if (target == null && dashboard.getServer(server + "1") != null) {
                        target = dashboard.getServerUtil().getServerByType(server);
                    }
                    if (target == null) {
                        tp.sendMessage(ChatColor.RED + "We are having trouble connecting you to that server! Try again soon.");
                        return;
                    }
                    if (!target.isOnline()) {
                        tp.sendMessage(ChatColor.RED + "We are having trouble connecting you to that server! Try again soon.");
                        return;
                    }
                    dashboard.getServerUtil().sendPlayer(tp, target);
                    break;
                }
                /*
                 * Tab Complete
                 */
                case 43: {
                    PacketTabComplete packet = new PacketTabComplete().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    int transactionId = packet.getTransactionId();
                    String command = packet.getCommand();
                    List<String> args = packet.getArgs();
                    List<String> results = packet.getResults();
                    Player tp = dashboard.getPlayer(uuid);
                    if (tp == null) {
                        return;
                    }
                    String last = "";
                    if (args.size() > 0) {
                        last = args.get(args.size() - 1);
                        if (!last.trim().startsWith(":")) {
                            last = "";
                        }
                    } else if (command.startsWith(":")) {
                        last = command;
                    }
                    if (last.isEmpty()) {
                        dashboard.getCommandUtil().tabComplete(tp, transactionId, command, args, results);
                    } else {
                        dashboard.getEmojiUtil().tabComplete(tp, transactionId, command, args, results);
                    }
                    break;
                }
                /*
                 * Set Player Resource Pack
                 */
                case 48: {
                    PacketSetPack packet = new PacketSetPack().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    String pack = packet.getPack();
                    Player tp = dashboard.getPlayer(uuid);
                    if (tp == null) {
                        return;
                    }
                    tp.setPack(pack);
                    break;
                }
                /*
                 * Get Player Resource Pack
                 */
                case 49: {
                    PacketGetPack packet = new PacketGetPack().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    Player tp = dashboard.getPlayer(uuid);
                    if (tp == null) {
                        return;
                    }
                    PacketGetPack send = new PacketGetPack(uuid, tp.getPack());
                    channel.send(send);
                    break;

                }
                /*
                 * Set Server Name
                 */
                case 52: {
                    PacketServerName packet = new PacketServerName().fromJSON(object);
                    String name = packet.getName();
                    channel.setServerName(name);
                    switch (channel.getType()) {
                        case BUNGEECORD: {
                            dashboard.getModerationUtil().sendMessage(ChatColor.GREEN +
                                    "A new BungeeCord instance (" + name + ") has connected to Dashboard.");
                            SlackMessage m = new SlackMessage("");
                            SlackAttachment a = new SlackAttachment("A new BungeeCord instance (" + name + ") has connected to Dashboard from the IP Address " +
                                    channel.remoteAddress().getAddress().toString());
                            a.color("good");
                            dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a));
                            break;
                        }
                        case INSTANCE: {
                            Server s = dashboard.getServer(name);
                            s.setOnline(true);
                            String running = "";
                            if (!s.getServerType().equals(s.getName())) {
                                running = " running " + s.getServerType();
                            }
                            if (!name.matches(MINIGAME_REGEX) && !dashboard.getServerUtil().isMuted(name)) {
                                dashboard.getModerationUtil().sendMessage(ChatColor.GREEN + "A new server instance (" + name + running +
                                        ") has connected to dashboard.");
                            }
                            if (s.isPark()) dashboard.getParkQueueManager().serverStartup(s);
                            break;
                        }
                    }
                    break;
                }
                /*
                 * WDL Protect
                 */
                case 54: {
                    PacketWDLProtect packet = new PacketWDLProtect().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    Player tp = dashboard.getPlayer(uuid);
                    final long timestamp = DateUtil.parseDateDiff("3d", true);
                    String username = "Unknown Username";
                    if (tp != null) {
                        username = tp.getUsername();
                        uuid = tp.getUniqueId();
                        tp.kickPlayer(ChatColor.RED + "Palace Network does not authorize the use of World Downloader Mods.\n" +
                                ChatColor.AQUA + "You have been temporarily banned for 3 Days.\n" + ChatColor.YELLOW +
                                "If you believe this was a mistake, send an appeal at " +
                                "https://palnet.us/appeal.");
                    }
                    Ban ban = new Ban(uuid, username, false, timestamp, "Attempting to use a World Downloader", "Dashboard");
                    dashboard.getMongoHandler().banPlayer(uuid, ban);
                    dashboard.getModerationUtil().announceBan(ban);
                    break;
                }
                /*
                 * Rank Change
                 */
                case 55: {
                    PacketRankChange packet = new PacketRankChange().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    Rank rank = packet.getRank();
                    List<String> tags = packet.getTags();
                    String source = packet.getSource();
                    Player player = dashboard.getPlayer(uuid);
                    if (player != null) player.send(new PacketPlayerRank(uuid, rank, tags));

                    dashboard.getSchedulerManager().runAsync(() -> {
                        String name;
                        if (player == null) {
                            name = dashboard.getMongoHandler().uuidToUsername(uuid);
                        } else {
                            PacketPlayerRank packet1 = new PacketPlayerRank(uuid, rank, tags);
                            player.send(packet1);
                            player.setRank(rank);
                            player.getTags().forEach(player::removeTag);
                            for (String tag : tags) {
                                player.addTag(RankTag.fromString(tag));
                            }
                            name = player.getUsername();
                            DashboardSocketChannel socketChannel = Dashboard.getInstance(player.getServer());
                            if (socketChannel != null) socketChannel.send(packet);

                            DiscordCacheInfo info = dashboard.getMongoHandler().getUserFromPlayer(player);
                            if (info != null) {
                                info.getMinecraft().setRank(rank.toString());
                                SocketConnection.sendUpdate(info);
                            }
                        }
                        List<RankTag> realTags = new ArrayList<>();
                        for (String s : tags) {
                            realTags.add(RankTag.fromString(s));
                        }
                        dashboard.getModerationUtil().rankChange(name, rank, realTags, source);

                        try {
                            int member_id = dashboard.getMongoHandler().getForumMemberId(uuid);
                            if (member_id != -1) {
                                dashboard.getForum().updatePlayerRank(uuid, member_id, rank, player);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    break;
                }
                /*
                 * Cross-server Warp
                 */
                case 56: {
                    network.palace.dashboard.packets.park.PacketWarp packet = new PacketWarp().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    String warp = packet.getWarp();
                    String serverType = packet.getServer();
                    Player tp = dashboard.getPlayer(uuid);
                    if (tp == null) {
                        return;
                    }
                    tp.setWarp(warp);
                    tp.setPendingWarp(true);
                    dashboard.getServerUtil().sendPlayerByType(tp, serverType);
                    break;
                }
                /*
                 * Empty Server
                 */
                case 57: {
                    PacketEmptyServer packet = new PacketEmptyServer().fromJSON(object);
                    String name = packet.getServer();
                    Server server = dashboard.getServer(name);
                    if (server == null) {
                        return;
                    }
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getServer().equals(server.getName())) {
                            Server target = dashboard.getServerUtil().getServerByType(name.replaceAll("\\d*$", ""), server.getUniqueId());
                            if (target == null) {
                                if (server.getServerType().equalsIgnoreCase("hub") || name.matches(MINIGAME_REGEX)) {
                                    target = dashboard.getServerUtil().getServerByType("Arcade");
                                } else {
                                    target = dashboard.getServerUtil().getServerByType("Hub");
                                }
                            }
                            if (target == null) {
                                target = dashboard.getServerUtil().getEmptyParkServer(server.isPark() ? server.getUniqueId() : null);
                            }
                            if (!target.getName().toLowerCase().startsWith("hub") && !target.getName().toLowerCase().startsWith("arcade")) {
                                tp.sendMessage(ChatColor.RED + "No fallback servers are available, so you were sent to a Park server.");
                            }
                            dashboard.getServerUtil().sendPlayer(tp, target);
                        }
                    }
                    break;
                }
                /*
                 * Inventory content
                 */
                case 58: {
                    dashboard.getSchedulerManager().runAsync(() -> {
                        PacketInventoryContent packet = new PacketInventoryContent().fromJSON(object);
                        Player player = dashboard.getPlayer(packet.getUuid());
                        if (!packet.isEmpty()) dashboard.getInventoryUtil().cacheInventory(packet.getUuid(), packet);
                        if (player == null) return;

                        if (packet.isDisconnect()) {
                            //Player is leaving current server
                            if (player.getServer().equalsIgnoreCase(channel.getServerName())) {
                                //Player is still on old server, need to store and send on server switch
                                player.setSendInventoryOnJoin(true);
                            } else {
                                player.setSendInventoryOnJoin(false);
                                //Player is on new server, check if need to send to that server
                                if (dashboard.getServer(player.getServer()).isInventory()) {
                                    //New server is a park server, needs inventory data
                                    DashboardSocketChannel socket = Dashboard.getInstance(player.getServer());
                                    if (socket == null) return;
                                    Resort resort = Resort.fromServer(player.getServer());
                                    ResortInventory inv = dashboard.getInventoryUtil().getInventory(player.getUuid(), resort);
                                    PacketInventoryContent updatePacket = new PacketInventoryContent(player.getUniqueId(), resort,
                                            inv.getBackpackJSON(), inv.getBackpackHash(), inv.getBackpackSize(),
                                            inv.getLockerJSON(), inv.getLockerHash(), inv.getLockerSize(),
                                            inv.getBaseJSON(), inv.getBaseHash(),
                                            inv.getBuildJSON(), inv.getBuildHash());
                                    socket.send(updatePacket);
//                                } else {
                                    //New server is not a park server, don't send inventory data
                                }
                            }
//                        } else {
                            //Packet only contains an update, player is still on current server
                        }
                    });
                    break;
                }
                /*
                 * Refresh Hotel Rooms
                 */
                case 59: {
                    PacketRefreshHotels packet = new PacketRefreshHotels().fromJSON(object);
                    sendAll(packet);
                    break;
                }
                /*
                 * Broadcast
                 */
                case 60: {
                    PacketBroadcast packet = new PacketBroadcast().fromJSON(object);
                    String message = packet.getMessage();
                    String source = packet.getSource();
                    String msg = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" + ChatColor.WHITE + "] " +
                            ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);
                    String staff = ChatColor.WHITE + "[" + ChatColor.AQUA + source + ChatColor.WHITE + "] " +
                            ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', message);
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (dashboard.getPlayer(tp.getUniqueId()).getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                            tp.sendMessage(staff);
                        } else {
                            tp.sendMessage(msg);
                        }
                    }
                    break;
                }
                /*
                 * Mute Chat
                 */
                case 61: {
                    PacketMuteChat packet = new PacketMuteChat().fromJSON(object);
                    String server = packet.getServer();
                    boolean muted = dashboard.getChatUtil().isChatMuted(server);
                    if (packet.isMute() == muted) {
                        return;
                    }
                    String msg;
                    if (!packet.isMute()) {
                        dashboard.getChatUtil().unmuteChat(server);
                        msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                                ChatColor.YELLOW + "Chat has been unmuted";
                    } else {
                        dashboard.getChatUtil().muteChat(server);
                        msg = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                                ChatColor.YELLOW + "Chat has been muted";
                    }
                    String msgname = msg + " by " + packet.getSource();
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if ((server.equals("ParkChat") && dashboard.getServer(tp.getServer()).isPark()) || tp.getServer().equals(server)) {
                            tp.sendMessage(tp.getRank().getRankId() >= Rank.TRAINEE.getRankId() ? msgname : msg);
                        }
                    }
                    break;
                }
                /*
                 * Refresh Warps
                 */
                case 62: {
                    PacketRefreshWarps packet = new PacketRefreshWarps().fromJSON(object);
                    sendAll(packet);
                    break;
                }
                /*
                 * Player List
                 */
                case 63: {
                    PacketPlayerList packet = new PacketPlayerList().fromJSON(object);
                    List<UUID> players = packet.getPlayers();
                    UUID uuid = channel.getBungeeID();
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getBungeeID().equals(uuid) && !players.contains(tp.getUniqueId())) {
                            dashboard.logout(tp.getUniqueId());
                            dashboard.getLogger().info("Player List Clean-Up: " + tp.getUsername() + "|" + tp.getUniqueId());
                        }
                    }
                    break;
                }
                /*
                 * Game status
                 */
                case 64: {
                    PacketGameStatus packet = new PacketGameStatus().fromJSON(object);
                    Server s = dashboard.getServerUtil().getServer(packet.getServerName());
                    if (s == null) return;
                    if (s.getName().equals("Arcade")) {
                        s.setArcade(true);
                        for (Server game : dashboard.getServerUtil().getServers()) {
                            if (!game.getName().matches(WebSocketServerHandler.MINIGAME_REGEX)) continue;
                            game.setGameNeedsUpdate(true);
                        }
                        break;
                    }
                    s.setGameState(packet.getState());
                    s.setCount(packet.getPlayerAmount());
                    s.setGameMaxPlayers(packet.getMaxPlayers());
                    s.setGameNeedsUpdate(true);
                    for (DashboardSocketChannel ch : Dashboard.getChannels(PacketConnectionType.ConnectionType.INSTANCE)) {
                        if (!ch.getServerName().startsWith("Arcade")) continue;
                        ch.send(packet);
                    }
                    break;
                }
                /*
                 * Bungee ID (Sent when a bungee changes IDs)
                 */
                case 65: {
                    PacketBungeeID packet = new PacketBungeeID().fromJSON(object);
                    UUID bid = packet.getBungeeID(); //Old ID
                    UUID nid = channel.getBungeeID(); //New ID
                    for (Player tp : dashboard.getOnlinePlayers()) {
                        if (tp.getBungeeID().equals(bid)) {
                            tp.setBungeeID(nid);
                        }
                    }
                    dashboard.getLogger().info("Bungee UUID updated for Bungee on " +
                            channel.localAddress().getAddress().toString() + " to " + nid);
                    break;
                }
                /*
                 * Player List Info (Import players from Bungee on dashboard reboot)
                 */
                case 66: {
                    PacketPlayerListInfo packet = new PacketPlayerListInfo().fromJSON(object);
                    List<PacketPlayerListInfo.Player> players = packet.getPlayers();
                    List<Player> list = new ArrayList<>();
                    for (PacketPlayerListInfo.Player p : players) {
                        if (dashboard.getPlayer(p.getUuid()) != null)
                            continue;
                        Player tp = new Player(p.getUuid(), p.getUsername(), p.getAddress(), p.getServer(),
                                channel.getBungeeID(), p.getMcVersion());
                        tp.setRank(Rank.fromString(p.getRank()));
                        if (p.getTags() != null && !p.getTags().isEmpty()) {
                            for (String s : p.getTags().split(";")) {
                                tp.addTag(RankTag.fromString(s));
                            }
                        }
                        list.add(tp);
                        dashboard.getServer(p.getServer()).changeCount(1);
                        dashboard.addPlayer(tp);
                        dashboard.getLogger().info("Player Join (BungeeJoin): " + tp.getUsername() + "|" + tp.getUniqueId());
                        dashboard.addToCache(tp.getUniqueId(), tp.getUsername());
                    }
                    dashboard.getSchedulerManager().runAsync(() -> list.forEach(p -> dashboard.getMongoHandler().login(p, true)));
                    break;
                }
                /*
                 * Confirm Player (return true if player is connected to dashboard, false if not)
                 */
                case 68: {
                    PacketConfirmPlayer packet = new PacketConfirmPlayer().fromJSON(object);
                    UUID uuid = packet.getUniqueId();
                    Player tp = dashboard.getPlayer(uuid);
                    boolean exists = tp != null;
                    if (!exists) {
                        // Check one more time if the player exists
                        exists = dashboard.hasPlayer(uuid);
                    }
                    if (!exists)
                        dashboard.getLogger().warning("Received request to verify player that doesn't exist " + uuid);
                    channel.send(new PacketConfirmPlayer(uuid, exists));
                    break;
                }
                /*
                 * Server Icon Request from BungeeCord
                 */
                case 70: {
                    if (!channel.getType().equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                        break;
                    }
                    PacketServerIcon packet = new PacketServerIcon(Launcher.getDashboard().getServerIconBase64());
                    channel.send(packet);
                    break;
                }
                /*
                 * I'm A Park
                 */
                case 71: {
                    dashboard.getServer(channel.getServerName()).setInventory(true);
                    break;
                }
                /*
                 * Log Statistic
                 */
                case 75: {
                    PacketLogStatistic packet = new PacketLogStatistic().fromJSON(object);
                    Point.Builder builder = Point.measurement(packet.getMeasurement())
                            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    for (Map.Entry<String, Object> entry : packet.getFields().entrySet()) {
                        Object o = entry.getValue();
                        if (o instanceof Integer) {
                            builder.addField(entry.getKey(), (int) entry.getValue());
                        } else if (o instanceof Double) {
                            builder.addField(entry.getKey(), (double) entry.getValue());
                        } else if (o instanceof Float) {
                            builder.addField(entry.getKey(), (float) entry.getValue());
                        } else if (o instanceof Short) {
                            builder.addField(entry.getKey(), (short) entry.getValue());
                        } else if (o instanceof Boolean) {
                            builder.addField(entry.getKey(), (boolean) entry.getValue());
                        } else if (o instanceof String) {
                            builder.addField(entry.getKey(), (String) entry.getValue());
                        }
                    }
                    for (Map.Entry<String, Object> entry : packet.getTags().entrySet()) {
                        builder.tag(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                    dashboard.getSchedulerManager().runAsync(() -> dashboard.getStatUtil().logDataPoint(builder.build()));
                    break;
                }
                /*
                 * Shareholder Show Request
                 */
                case 77: {
                    PacketShowRequest packet = new PacketShowRequest().fromJSON(object);
                    UUID uuid = packet.getUuid();
                    Player player = dashboard.getPlayer(uuid);
                    if (player == null) return;
                    String show = packet.getShowName();
                    String server = packet.getServer();

                    dashboard.getSchedulerManager().runAsync(() -> {
                        if (dashboard.getShowUtil().checkPlayer(player)) {
                            BaseComponent[] comp = new ComponentBuilder("[").color(ChatColor.WHITE)
                                    .append("Dashboard").color(ChatColor.RED)
                                    .append("] ").color(ChatColor.WHITE)
                                    .append(player.getUsername() + " ").color(ChatColor.LIGHT_PURPLE)
                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server))
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new ComponentBuilder("Click to run ").color(ChatColor.GREEN)
                                                    .append("/server " + server).color(ChatColor.YELLOW).create()))
                                    .append("has requested to start ").color(ChatColor.AQUA)
                                    .append(show + ", ").color(ChatColor.GREEN)
                                    .append("please accept or deny this request on ").color(ChatColor.AQUA)
                                    .append(server + " ").color(ChatColor.GREEN)
                                    .append("using ").color(ChatColor.AQUA)
                                    .append("/shows").color(ChatColor.GREEN).create();

                            boolean noStaff = true;
                            for (Player tp : dashboard.getOnlinePlayers()) {
                                if (tp.getRank().getRankId() >= Rank.TRAINEE.getRankId()) {
                                    tp.sendMessage(comp);
                                    noStaff = false;
                                }
                            }

                            if (noStaff) {
                                player.sendMessage(ChatColor.RED + "There are no staff members online right now! Try again soon.");
                                return;
                            }

                            player.sendMessage(ChatColor.AQUA + "Your request to start " + ChatColor.GREEN + show + ChatColor.AQUA +
                                    " has been received, please allow a moment for a staff member to respond to your request.");

                            channel.send(new PacketShowRequestResponse(packet.getRequestId()));
                        }
                    });
                    break;
                }
                /*
                 * Create VirtualQueue
                 */
                case 81: {
                    CreateQueuePacket packet = new CreateQueuePacket().fromJSON(object);
                    if (!channel.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) return;
                    Server server = dashboard.getServer(channel.getServerName(), true);
                    if (server != null) dashboard.getParkQueueManager().createQueue(packet, server);
                    break;
                }
                /*
                 * Remove VirtualQueue
                 */
                case 82: {
                    RemoveQueuePacket packet = new RemoveQueuePacket().fromJSON(object);
                    if (!channel.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) return;
                    Server server = dashboard.getServer(channel.getServerName(), true);
                    if (server != null) dashboard.getParkQueueManager().removeQueue(packet, server);
                    break;
                }
                /*
                 * Update VirtualQueue
                 */
                case 83: {
                    UpdateQueuePacket packet = new UpdateQueuePacket().fromJSON(object);
                    if (!channel.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) return;
                    Server server = dashboard.getServer(channel.getServerName(), true);
                    if (server != null) dashboard.getParkQueueManager().updateQueue(packet, server);
                    break;
                }
                /*
                 * Admit VirtualQueue
                 */
                case 84: {
                    AdmitQueuePacket packet = new AdmitQueuePacket().fromJSON(object);
                    if (!channel.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) return;
                    Server server = dashboard.getServer(channel.getServerName(), true);
                    if (server != null) dashboard.getParkQueueManager().admitQueue(packet, server);
                    break;
                }
                /*
                 * Announce VirtualQueue
                 */
                case 85: {
                    AnnounceQueuePacket packet = new AnnounceQueuePacket().fromJSON(object);
                    if (!channel.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) return;
                    Server server = dashboard.getServer(channel.getServerName(), true);
                    if (server != null) dashboard.getParkQueueManager().announceQueue(packet, server);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Dashboard dashboard = Launcher.getDashboard();
        DashboardSocketChannel dash = (DashboardSocketChannel) ctx.channel();
        if (dash.getType().equals(PacketConnectionType.ConnectionType.WEBCLIENT)) return;
        boolean devs = false;
        for (Player tp : dashboard.getOnlinePlayers()) {
            if (tp.getRank().getRankId() >= Rank.DEVELOPER.getRankId()) {
                devs = true;
                break;
            }
        }
        String addon = "";
        if (!devs) addon = " No Developer or Manager is online, please notify one.";
        switch (dash.getType()) {
            case BUNGEECORD: {
                dashboard.getModerationUtil().sendMessage(ChatColor.RED +
                        "A BungeeCord instance (" + dash.getServerName() + ") has disconnected from dashboard!" + addon);
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("A BungeeCord instance (" + dash.getServerName() + ") has disconnected from dashboard! #devs");
                a.color("danger");
                dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a));
                break;
            }
            case DAEMON: {
                dashboard.getModerationUtil().sendMessage(ChatColor.RED +
                        "A daemon has disconnected from Dashboard!" + addon);
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("A daemon has disconnected from Dashboard! #devs");
                a.color("danger");
                dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a));
                break;
            }
            case WEBCLIENT: {
                break;
            }
            case INSTANCE: {
                String name = dash.getServerName();
                Server s = dashboard.getServer(name, true);
                if (s == null) {
                    return;
                }
                String running = "";
                if (!s.getServerType().equals(s.getName())) {
                    running = " running " + s.getServerType();
                }
                s.setOnline(false);
                s.setInventory(false);
                s.setGameState(GameState.LOBBY);
                s.setGameNeedsUpdate(true);
                s.setArcade(false);
                if (name.matches(MINIGAME_REGEX)) {
                    PacketGameStatus packet = new PacketGameStatus(GameState.RESTARTING, 0, 0, name);
                    for (DashboardSocketChannel ch : Dashboard.getChannels(PacketConnectionType.ConnectionType.INSTANCE)) {
                        if (!ch.getServerName().startsWith("Arcade")) continue;
                        ch.send(packet);
                    }
                } else {
                    if (!dashboard.getServerUtil().isMuted(name)) {
                        dashboard.getModerationUtil().sendMessage(ChatColor.RED +
                                "A server instance (" + name + running + ") has disconnected from Dashboard!" + addon);
                        SlackMessage m = new SlackMessage("");
                        SlackAttachment a = new SlackAttachment("A server instance (" + name + running +
                                ") has disconnected from Dashboard! #devs");
                        a.color("danger");
                        dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a));
                    }
                }
                break;
            }
            case AUDIOSERVER: {
                dashboard.getModerationUtil().sendMessage(ChatColor.RED +
                        "The Audio Server has disconnected from Dashboard!" + addon);
                SlackMessage m = new SlackMessage("");
                SlackAttachment a = new SlackAttachment("The Audio Server has disconnected from Dashboard! #devs");
                a.color("danger");
                dashboard.getSlackUtil().sendDashboardMessage(m, Collections.singletonList(a));
                break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Dashboard dashboard = Launcher.getDashboard();
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            try {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendAll(BasePacket packet) {
        Dashboard dashboard = Launcher.getDashboard();

        for (Object o : WebSocketServerHandler.getGroup()) {
            DashboardSocketChannel dash = (DashboardSocketChannel) o;
            if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                continue;
            }
            try {
                if (dashboard.getServer(dash.getServerName()).isPark()) {
                    dash.send(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendInventoryUpdate(String instance, BasePacket packet) {
        try {
            DashboardSocketChannel socketChannel = Dashboard.getInstance(instance);
            if (socketChannel == null) return;
            socketChannel.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Find all servers that function as arcades
     *
     * @return a list of server names
     */
    private List<String> findArcadeServers() {
        Dashboard dashboard = Launcher.getDashboard();
        return dashboard.getServers().stream().filter(server -> server.getName().startsWith("Arcade"))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collection::stream)).map(Server::getName).collect(Collectors.toList());
    }
}