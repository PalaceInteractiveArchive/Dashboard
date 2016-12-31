package com.palacemc.dashboard.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import com.palacemc.dashboard.handlers.*;
import com.palacemc.dashboard.packets.audio.PacketContainer;
import com.palacemc.dashboard.packets.audio.PacketGetPlayer;
import com.palacemc.dashboard.packets.audio.PacketPlayerInfo;
import com.palacemc.dashboard.packets.bungee.PacketBungeeID;
import com.palacemc.dashboard.packets.bungee.PacketPlayerListInfo;
import com.palacemc.dashboard.packets.dashboard.*;
import com.palacemc.dashboard.packets.park.*;
import com.palacemc.dashboard.slack.SlackAttachment;
import com.palacemc.dashboard.slack.SlackMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.*;

/**
 * Created by Marc on 6/15/15
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private WebSocketServerHandshaker handshaker;

    public static ChannelGroup getGroup() {
        return channels;
    }

    private Dashboard dashboard = Launcher.getDashboard();

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
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(null, null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
            final DashboardSocketChannel channel = (DashboardSocketChannel) ctx.channel();
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        } else if (frame instanceof PongWebSocketFrame) {
            return;
        } else if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%slackService frame types not supported",
                    frame.getClass().getName()));
        }

        String request = ((TextWebSocketFrame) frame).text();
        JsonObject object = (JsonObject) new JsonParser().parse(request);

        if (!object.has("id")) {
            return;
        }

        int id = object.get("id").getAsInt();
        DashboardSocketChannel channel = (DashboardSocketChannel) ctx.channel();

        Player player;

        SlackMessage message = new SlackMessage("");
        SlackAttachment attachment;

        switch (id) {
            case 13:
                PacketGetPlayer packet = new PacketGetPlayer().fromJSON(object);
                String username = packet.getPlayerName();

                player = dashboard.getPlayer(username);
                PacketPlayerInfo info;

                if (player == null || player.getAudioAuth() == -1) {
                    info = new PacketPlayerInfo(null, username, 0, "");
                } else {
                    info = new PacketPlayerInfo(
                            player.getUuid(), username, player.getAudioAuth(), player.getServer());
                    player.resetAudioAuth();
                    try {
                        PacketAudioConnect connect = new PacketAudioConnect(player.getUuid());
                        dashboard.getInstance(player.getServer()).send(connect);
                    } catch (Exception ignored) {
                    }
                }
                channel.send(info);
                break;
            case 17:
                PacketContainer audioContainer = new PacketContainer().fromJSON(object);

                for (Object socketChannel : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) socketChannel;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.AUDIOSERVER)) {
                        continue;
                    }
                    dash.send(audioContainer);
                }
                break;

            case 22:
                PacketConnectionType connectionTypePacket = new PacketConnectionType().fromJSON(object);
                PacketConnectionType.ConnectionType type = connectionTypePacket.getType();
                channel.setType(type);

                switch (type) {
                    case BUNGEECORD:
                        dashboard.getModerationUtil().sendMessage(ChatColor.GREEN +
                                "A new BungeeCord instance has connected to dashboard.");

                        attachment = new SlackAttachment("A new BungeeCord instance has connected to Dashboard from the IP Address " +
                                channel.remoteAddress().getAddress().toString());
                        attachment.color("good");

                        dashboard.getSlackUtil().sendDashboardMessage(message, Collections.singletonList(attachment));
                        break;
                    case DAEMON:
                        dashboard.getModerationUtil().sendMessage(ChatColor.GREEN +
                                "A new daemon has connected to Dashboard.");

                        attachment = new SlackAttachment("A new daemon has connected to Dashboard from the IP Address " +
                                channel.remoteAddress().getAddress().toString());
                        attachment.color("good");

                        dashboard.getSlackUtil().sendDashboardMessage(message, Collections.singletonList(attachment));
                        break;
                    case WEBCLIENT:
                        break;
                    case INSTANCE:
                        break;
                    case AUDIOSERVER:
                        dashboard.getModerationUtil().sendMessage(ChatColor.GREEN +
                                "The Audio Server has connected to Dashboard.");

                        attachment = new SlackAttachment("The Audio Server has connected to Dashboard from the IP Address " +
                                channel.remoteAddress().getAddress().toString());
                        attachment.color("good");

                        dashboard.getSlackUtil().sendDashboardMessage(message, Collections.singletonList(attachment));
                        break;
                }

                dashboard.getLogger().info("New " + type.name().toLowerCase() + " connection");

                if (type.equals(PacketConnectionType.ConnectionType.BUNGEECORD)) {
                    PacketUpdateMOTD motd = new PacketUpdateMOTD(dashboard.getMotd(), dashboard.getMotdMaintenance(),
                            dashboard.getInfo());
                    PacketOnlineCount count = new PacketOnlineCount(dashboard.getOnlinePlayers().size());

                    List<String> servers = new ArrayList<>();

                    for (Server server : dashboard.getServerUtil().getServers()) {
                        servers.add(server.getName() + ":" + server.getAddress() + ":" + server.getPort());
                    }

                    PacketServerList server = new PacketServerList(servers);
                    PacketTargetLobby lobby = new PacketTargetLobby(dashboard.getTargetServer());
                    PacketCommandList commands = new PacketCommandList(new ArrayList<>(dashboard.getCommandUtil().getCommandsAndAliases()));
                    PacketMaintenance maintenance = new PacketMaintenance(dashboard.isMaintenance());
                    PacketBungeeID bungeeID = new PacketBungeeID(channel.getBungeeID());

                    channel.send(motd);
                    channel.send(count);
                    channel.send(server);
                    channel.send(lobby);
                    channel.send(commands);
                    channel.send(maintenance);
                    channel.send(bungeeID);

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
            case 23:
                PacketPlayerJoin playerJoinPacket = new PacketPlayerJoin().fromJSON(object);
                player = new Player(playerJoinPacket.getUuid(), playerJoinPacket.getUsername(), playerJoinPacket.getAddress(),
                        playerJoinPacket.getServer(), channel.getBungeeID());

                dashboard.getSqlUtil().login(player);
                break;
            case 24:
                PacketPlayerDisconnect playerDisconnectPacket = new PacketPlayerDisconnect().fromJSON(object);

                dashboard.logout(playerDisconnectPacket.getUuid());
                break;
            case 25:
                PacketPlayerChat playerChatPacket = new PacketPlayerChat().fromJSON(object);

                dashboard.getChatUtil().chatEvent(playerChatPacket);
                break;
            case 26:
                PacketMessage messagePacket = new PacketMessage().fromJSON(object);

                UUID uuid = messagePacket.getUuid();
                String msg = messagePacket.getMessage();
                player = dashboard.getPlayer(uuid);

                if (player == null) return;

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                break;
            case 27:
                PacketServerSwitch serverSwitchPacket = new PacketServerSwitch().fromJSON(object);

                uuid = serverSwitchPacket.getUuid();
                String target = serverSwitchPacket.getTarget();
                player = dashboard.getPlayer(uuid);

                if (player == null) return;

                // New connection
                if (dashboard.getServerUtil().getServer(player.getServer()) == null) {
                    dashboard.getServerUtil().getServer(target).changeCount(1);

                    player.setServer(target);

                    if (dashboard.getServer(target).isPark() && dashboard.getInstance(target) != null) {
                        player.setInventoryUploaded(false);
                        PacketInventoryStatus update = new PacketInventoryStatus(player.getUuid(), 1);
                        try {
                            dashboard.getInstance(target).send(update);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (player.isNewGuest()) {
                        Timer tutorial = new Timer();

                        final Player tutorialPlayer = player;

                        tutorial.schedule(new TimerTask() {
                            int i = 0;
                            @Override
                            public void run() {
                                switch (i) {
                                    case 0:
                                        tutorialPlayer.sendMessage(ChatColor.GREEN + "\nWelcome to the " + ChatColor.AQUA + "Palace Network" +
                                                ChatColor.GREEN + ", we're happy you're here!");
                                        tutorialPlayer.mention();
                                        break;
                                    case 4:
                                        tutorialPlayer.sendMessage(ChatColor.GREEN + "\nWe are an all-inclusive family-friendly " +
                                                ChatColor.DARK_GREEN + "Minecraft " + ChatColor.GREEN + "gaming network!");
                                        tutorialPlayer.mention();
                                        break;
                                    case 7:
                                        tutorialPlayer.sendMessage(ChatColor.GREEN + "\nRight now you're at the " + ChatColor.AQUA +
                                                "Transportation and Ticket Center. " + ChatColor.GREEN +
                                                "From here, you can board a monorail, bus or ferryboat to all Parks and Resorts.");
                                        tutorialPlayer.mention();
                                        break;
                                    case 15:
                                        tutorialPlayer.sendMessage(ChatColor.GREEN + "\nYou can also use your " + ChatColor.AQUA +
                                                "MagicBand " + ChatColor.GREEN + "to navigate to different parts of " +
                                                "our server such as Creative or the Arcade");
                                        tutorialPlayer.mention();
                                        break;
                                    case 22:
                                        tutorialPlayer.sendMessage(ChatColor.GREEN + "\nInstall our Resource Pack for the " +
                                                ChatColor.AQUA + "best " + ChatColor.GREEN +
                                                "experience possible! All you have to do is type " + ChatColor.AQUA +
                                                "/pack " + ChatColor.GREEN + "and select " + ChatColor.LIGHT_PURPLE +
                                                "Main. " + ChatColor.GRAY + "" + ChatColor.ITALIC +
                                                "(You can set this up when the tutorial finishes)");
                                        tutorialPlayer.mention();
                                        break;
                                    case 32:
                                        tutorialPlayer.sendMessage(ChatColor.GREEN + "\nAlso, connect to our " + ChatColor.BLUE +
                                                "Audio Server " + ChatColor.GREEN + "for an immersive experience! You will hear the " +
                                                ChatColor.AQUA + "sounds from rides, music from shows, and so much more! " +
                                                ChatColor.GREEN + "Just type " + ChatColor.AQUA + "/audio " + ChatColor.GREEN +
                                                "and click the message to connect. " + ChatColor.GRAY + "" + ChatColor.ITALIC +
                                                "(You can set this up when the tutorial finishes)");
                                        tutorialPlayer.mention();
                                        break;
                                    case 43:
                                        tutorialPlayer.sendMessage(ChatColor.GREEN + "\nBefore you start exploring, please take a " +
                                                "few minutes to review our rules: " + ChatColor.AQUA +
                                                "mcmagic.us/rules " + ChatColor.GREEN + "\nWe are a " +
                                                "family-friendly server with a goal of providing a safe, fun experience " +
                                                "to all of our Guests.");
                                        tutorialPlayer.mention();
                                        break;
                                    case 52:
                                        tutorialPlayer.sendMessage(ChatColor.GREEN + "\nAfter you finish reviewing our rules, " +
                                                "you're finished with the tutorial! " + ChatColor.DARK_AQUA +
                                                "Note: New Guests must wait " + ChatColor.BOLD + "15 minutes " +
                                                ChatColor.DARK_AQUA + "before using chat. Read why: " +
                                                ChatColor.AQUA + "mcmagic.us/rules#chat");
                                        tutorialPlayer.mention();
                                        tutorialPlayer.setNewGuest(false);
                                        dashboard.getSqlUtil().completeTutorial(tutorialPlayer.getUuid());
                                        cancel();
                                }
                                i++;
                            }
                        }, 2000, 1000);

                        player.setTutorial(tutorial);
                    }
                    break;
                }

                // Going to Park server
                if (dashboard.getServer(target).isPark()) {
                    // Leaving non-Park server or inventory is uploaded from Park server
                    if (!dashboard.getServer(player.getServer()).isPark() || player.isInventoryUploaded()) {
                        player.setInventoryUploaded(false);
                        PacketInventoryStatus update = new PacketInventoryStatus(player.getUuid(), 1);
                        try {
                            dashboard.getInstance(target).send(update);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (player.isPendingWarp()) {
                        player.chat("/warp " + player.getWarp());
                        player.setPendingWarp(false);
                    }
                }
                com.palacemc.dashboard.packets.audio.PacketServerSwitch change =
                        new com.palacemc.dashboard.packets.audio.PacketServerSwitch(target);

                PacketContainer audio = new PacketContainer(uuid, change.getJSON().toString());

                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.AUDIOSERVER)) {
                        continue;
                    }
                    dash.send(audio);
                }

                if (!player.getServer().equalsIgnoreCase("unknown")) {
                    dashboard.getServerUtil().getServer(player.getServer()).changeCount(-1);
                }

                dashboard.getServerUtil().getServer(target).changeCount(1);
                player.setServer(target);
                break;
            case 32:
                PacketSendToServer sendToServerPacket = new PacketSendToServer().fromJSON(object);

                uuid = sendToServerPacket.getUuid();
                String serverName = sendToServerPacket.getServer();
                player = dashboard.getPlayer(uuid);

                if (player == null) {
                    return;
                }

                Server targetServer = dashboard.getServer(serverName);

                if (targetServer == null) {
                    player.sendMessage(ChatColor.RED + "We are having trouble connecting you to that server! Try again soon.");
                    return;
                }

                if (dashboard.getServer(serverName + "1") != null) {
                    targetServer = dashboard.getServerUtil().getServerByType(serverName);
                }

                if (!targetServer.isOnline()) {
                    player.sendMessage(ChatColor.RED + "We are having trouble connecting you to that server! Try again soon.");
                    return;
                }
                dashboard.getServerUtil().sendPlayer(player, targetServer.getName());
                break;
            case 43:
                PacketTabComplete tabCompletePacket = new PacketTabComplete().fromJSON(object);

                uuid = tabCompletePacket.getUuid();
                String command = tabCompletePacket.getCommand();
                List<String> args = tabCompletePacket.getArgs();
                List<String> results = tabCompletePacket.getResults();

                player = dashboard.getPlayer(uuid);

                if (player == null) {
                    return;
                }

                dashboard.getCommandUtil().tabComplete(player, command, args, results);
                break;
            case 48: {
                PacketSetPack setPackPacket = new PacketSetPack().fromJSON(object);
                uuid = setPackPacket.getUuid();
                String pack = setPackPacket.getPack();
                player = dashboard.getPlayer(uuid);

                if (player == null) {
                    return;
                }

                player.setPack(pack);
                break;
            }
            case 49:
                PacketGetPack getPackPacket = new PacketGetPack().fromJSON(object);

                uuid = getPackPacket.getUuid();
                player = dashboard.getPlayer(uuid);

                if (player == null) {
                    return;
                }

                PacketGetPack send = new PacketGetPack(uuid, player.getPack());
                channel.send(send);
                break;
            case 52:
                PacketServerName serverNamePacket = new PacketServerName().fromJSON(object);

                String name = serverNamePacket.getName();

                channel.setServerName(name);

                Server s = dashboard.getServer(name);
                s.setOnline(true);

                String running = "";

                if (!s.getServerType().equals(s.getName())) {
                    running = " running " + s.getServerType();
                }

                if (!name.matches("[a-z]\\d{1,3}")) {
                    dashboard.getModerationUtil().sendMessage(ChatColor.GREEN + "A new server instance (" + name + running +
                            ") has connected to Dashboard.");
                }
                break;
            case 54:
                PacketWDLProtect wdlPacket = new PacketWDLProtect().fromJSON(object);

                uuid = wdlPacket.getUuid();
                player = dashboard.getPlayer(uuid);
                Ban ban;

                if (player != null) {
                    ban = new Ban(player.getUuid(), player.getUsername(), false, System.currentTimeMillis() + 259200000,
                            "Attempting to use a World Downloader", "Dashboard");
                    player.kickPlayer(ChatColor.RED + "Palace Network does not authorize the use of World Downloader Mods!\n" +
                            ChatColor.AQUA + "You have been temporarily banned for 3 Days.\n" + ChatColor.YELLOW +
                            "If you believe this was a mistake, send an appeal at " +
                            "palace.network/appeal.");
                } else {
                    ban = new Ban(uuid, "Unknown Username", false, System.currentTimeMillis() + 259200000,
                            "Attempting to use a World Downloader", "Dashboard");
                }
                dashboard.getSqlUtil().banPlayer(uuid, ban.getReason(), true, new Date(System.currentTimeMillis()),
                        ban.getSource());
                dashboard.getModerationUtil().announceBan(ban);
                break;
            case 55:
                PacketRankChange rankChangePacket = new PacketRankChange().fromJSON(object);

                dashboard.getSchedulerManager().runAsync(() -> {
                    String playerName;

                    UUID playerUUID = rankChangePacket.getUuid();
                    Player changedPlayer = dashboard.getPlayer(playerUUID);
                    final Rank rank = rankChangePacket.getRank();
                    final String source = rankChangePacket.getSource();


                    if (changedPlayer != null) {
                        PacketPlayerRank packet1 = new PacketPlayerRank(playerUUID, rank);
                        changedPlayer.send(packet1);
                        changedPlayer.setRank(rank);
                        playerName = changedPlayer.getUsername();
                    } else {
                        playerName = dashboard.getSqlUtil().usernameFromUUID(playerUUID);
                    }
                    dashboard.getModerationUtil().rankChange(playerName, rank, source);
                });
                break;
            case 56:
                com.palacemc.dashboard.packets.park.PacketWarp warpPacket = new com.palacemc.dashboard.packets.park.PacketWarp().fromJSON(object);

                uuid = warpPacket.getUuid();
                String warp = warpPacket.getWarp();
                String serverType = warpPacket.getServer();
                player = dashboard.getPlayer(uuid);

                if (player == null) {
                    return;
                }

                player.setWarp(warp);
                player.setPendingWarp(true);

                dashboard.getServerUtil().sendPlayerByType(player, serverType);
                break;
            case 57:
                PacketEmptyServer emptyServerPacket = new PacketEmptyServer().fromJSON(object);

                serverName = emptyServerPacket.getServer();
                Server server = dashboard.getServer(serverName);

                if (server == null) {
                    return;
                }

                for (Player onlinePlayer : dashboard.getOnlinePlayers()) {
                    if (onlinePlayer.getServer().equals(server.getName())) {
                        Server serverTarget = dashboard.getServerUtil().getServerByType(serverName.replaceAll("\\d*$", ""), server.getUuid());
                        if (serverTarget == null) {
                            if (server.getServerType().equalsIgnoreCase("hub")) {
                                serverTarget = dashboard.getServerUtil().getServerByType("Arcade");
                            } else {
                                serverTarget = dashboard.getServerUtil().getServerByType("hub");
                            }
                        }
                        if (serverTarget == null) {
                            serverTarget = dashboard.getServerUtil().getEmptyParkServer(server.isPark() ? server.getUuid() : null);
                        }
                        if (!serverTarget.getName().toLowerCase().startsWith("hub") && !serverTarget.getName().toLowerCase().startsWith("arcade")) {
                            onlinePlayer.sendMessage(ChatColor.RED + "No fallback server available. Sending to parks.");
                        }
                        dashboard.getServerUtil().sendPlayer(onlinePlayer, serverTarget.getName());
                    }
                }
                break;
            case 58:
                PacketInventoryStatus inventoryStatusPacket = new PacketInventoryStatus().fromJSON(object);

                uuid = inventoryStatusPacket.getUuid();
                int status = inventoryStatusPacket.getStatus();
                serverName = channel.getServerName();
                player = dashboard.getPlayer(uuid);

                if (player == null || serverName.equals("") || status != 0 || !dashboard.getServer(player.getServer()).isPark()) {
                    return;
                }

                if (!player.getServer().equals(serverName)) {
                    PacketInventoryStatus update = new PacketInventoryStatus(player.getUuid(), 1);
                    DashboardSocketChannel socketChannel = dashboard.getInstance(player.getServer());
                    if (socketChannel == null) {
                        dashboard.getLogger().warn("Target server " + player.getServer() +
                                " not connected, could not complete inventory update!");
                    } else {
                        socketChannel.send(update);
                    }
                } else {
                    player.setInventoryUploaded(true);
                }
                break;
            case 59:
                PacketRefreshHotels refreshHotelsPacket = new PacketRefreshHotels().fromJSON(object);
                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;
                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                        continue;
                    }
                    try {
                        if (dashboard.getServer(dash.getServerName()).isPark()) {
                            dash.send(refreshHotelsPacket);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 60:
                PacketBroadcast broadcastPacket = new PacketBroadcast().fromJSON(object);

                String broadcastMesage = broadcastPacket.getMessage();
                String sourceServer = broadcastPacket.getSource();

                String finalMessage = ChatColor.WHITE + "[" + ChatColor.AQUA + "Information" + ChatColor.WHITE + "] " +
                        ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', broadcastMesage);

                String staff = ChatColor.WHITE + "[" + ChatColor.AQUA + sourceServer + ChatColor.WHITE + "] " +
                        ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', broadcastMesage);

                for (Player onlinePlayer : dashboard.getOnlinePlayers()) {
                    if (dashboard.getPlayer(onlinePlayer.getUuid()).getRank().getRankId() >= Rank.KNIGHT.getRankId()) {
                        onlinePlayer.sendMessage(staff);
                    } else {
                        onlinePlayer.sendMessage(finalMessage);
                    }
                }
                break;
            case 61:
                PacketMuteChat muteChatPacket = new PacketMuteChat().fromJSON(object);

                serverName = muteChatPacket.getServer();
                boolean muted = dashboard.getChatUtil().isChatMuted(serverName);

                if (muteChatPacket.isMute() == muted) {
                    return;
                }

                String muteMessage;

                if (!muteChatPacket.isMute()) {
                    dashboard.getChatUtil().unmuteChat(serverName);
                    muteMessage = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                            ChatColor.YELLOW + "Chat has been unmuted";
                } else {
                    dashboard.getChatUtil().muteChat(serverName);
                    muteMessage = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "Palace Chat" + ChatColor.WHITE + "] " +
                            ChatColor.YELLOW + "Chat has been muted";
                }

                for (Player onlinePlayer : dashboard.getOnlinePlayers()) {
                    if ((serverName.equals("ParkChat") && dashboard.getServer(onlinePlayer.getServer()).isPark()) || onlinePlayer.getServer().equals(serverName)) {
                        onlinePlayer.sendMessage(muteMessage);
                    }
                }
                break;
            case 62:
                PacketRefreshWarps refreshWarpsPacket = new PacketRefreshWarps().fromJSON(object);

                for (Object o : WebSocketServerHandler.getGroup()) {
                    DashboardSocketChannel dash = (DashboardSocketChannel) o;

                    if (!dash.getType().equals(PacketConnectionType.ConnectionType.INSTANCE)) {
                        continue;
                    }

                    try {
                        if (dashboard.getServer(dash.getServerName()).isPark()) {
                            dash.send(refreshWarpsPacket);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 63:
                PacketPlayerList playerListPacket = new PacketPlayerList().fromJSON(object);

                List<UUID> players = playerListPacket.getPlayers();
                uuid = channel.getBungeeID();

                for (Player onlinePlayer : dashboard.getOnlinePlayers()) {
                    if (onlinePlayer.getBungeeID().equals(uuid) && !players.contains(onlinePlayer.getUuid())) {
                        dashboard.logout(onlinePlayer.getUuid());
                    }
                }
                break;
            case 65:
                PacketBungeeID bungeeIDPacket = new PacketBungeeID().fromJSON(object);

                UUID oldID = bungeeIDPacket.getBungeeId();
                UUID newID = channel.getBungeeID();

                for (Player onlinePlayer : dashboard.getOnlinePlayers()) {
                    if (onlinePlayer.getBungeeID().equals(oldID)) {
                        onlinePlayer.setBungeeID(newID);
                    }
                }

                dashboard.getLogger().info("Bungee UUID updated for Bungee on " +
                        channel.localAddress().getAddress().toString());
                break;
            case 66:
                PacketPlayerListInfo playerListInfoPacket = new PacketPlayerListInfo().fromJSON(object);

                List<PacketPlayerListInfo.Player> playerList = playerListInfoPacket.getPlayers();
                List<Player> list = new ArrayList<>();

                for (PacketPlayerListInfo.Player playerInList : playerList) {
                    player = new Player(
                            playerInList.getUuid(), playerInList.getUsername(), playerInList.getAddress(),
                            playerInList.getServer(), channel.getBungeeID());
                    player.setRank(Rank.fromString(playerInList.getRank()));
                    list.add(player);
                }

                final List<Player> finalList = list;
                dashboard.getSchedulerManager().runAsync(() ->
                        finalList.forEach(listedPlayer -> dashboard.getSqlUtil().silentJoin(listedPlayer)));
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SlackMessage message = new SlackMessage("");
        SlackAttachment attachment;

        super.channelActive(ctx);
        DashboardSocketChannel dash = (DashboardSocketChannel) ctx.channel();
        if (dash.getType().equals(PacketConnectionType.ConnectionType.WEBCLIENT)) {
            return;
        }
        boolean devs = false;
        for (Player player : dashboard.getOnlinePlayers()) {
            if (player.getRank().getRankId() >= Rank.WIZARD.getRankId()) {
                devs = true;
                break;
            }
        }
        String addon = "";
        if (!devs) {
            addon = " No Developer or Manager is online, please notify one.";
        }
        switch (dash.getType()) {
            case BUNGEECORD:
                dashboard.getModerationUtil().sendMessage(ChatColor.RED +
                        "A BungeeCord instance has disconnected from Dashboard!" + addon);

                attachment = new SlackAttachment("A BungeeCord Instance has disconnected from Dashboard! #devs");
                attachment.color("danger");

                dashboard.getSlackUtil().sendDashboardMessage(message, Collections.singletonList(attachment));
                break;
            case DAEMON:
                dashboard.getModerationUtil().sendMessage(ChatColor.RED +
                        "A daemon has disconnected from Dashboard!" + addon);

                attachment = new SlackAttachment("A daemon has disconnected from Dashboard! #devs");
                attachment.color("danger");

                dashboard.getSlackUtil().sendDashboardMessage(message, Collections.singletonList(attachment));
                break;
            case WEBCLIENT:
                break;
            case INSTANCE:
                String name = dash.getServerName();
                Server s = dashboard.getServer(name);
                String running = "";

                if (!s.getServerType().equals(s.getName())) {
                    running = " running " + s.getServerType();
                }

                if (!name.matches("[a-z]\\d{1,3}")) {
                    dashboard.getModerationUtil().sendMessage(ChatColor.RED +
                            "A server instance (" + name + running + ") has disconnected from Dashboard!" + addon);

                    attachment = new SlackAttachment("A server instance (" + name + running +
                            ") has disconnected from Dashboard! #devs");
                    attachment.color("danger");

                    dashboard.getSlackUtil().sendDashboardMessage(message, Collections.singletonList(attachment));
                }
                break;
            case AUDIOSERVER:
                dashboard.getModerationUtil().sendMessage(ChatColor.RED +
                        "The Audio Server has disconnected from Dashboard!" + addon);
                attachment = new SlackAttachment("The Audio Server has disconnected from Dashboard! #devs");
                attachment.color("danger");
                dashboard.getSlackUtil().sendDashboardMessage(message, Collections.singletonList(attachment));
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            try {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            } catch (Exception e) {
                dashboard.getLogger().warn(e.getMessage(), e);
            }
        }
    }
}