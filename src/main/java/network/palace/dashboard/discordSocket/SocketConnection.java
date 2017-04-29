package network.palace.dashboard.discordSocket;

import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.Getter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.ChatColor;

import java.net.URISyntaxException;

public class SocketConnection {

    @Getter private static Gson gson = new Gson();
    private static Socket socket = null;

    public SocketConnection() {
        Dashboard dashboard = Launcher.getDashboard();
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnectionAttempts = 1000000;
            socket = IO.socket(dashboard.getSocketURL(), options);
        } catch (URISyntaxException e) {
            dashboard.getLogger().info(ChatColor.DARK_RED + "Discord link socket uri syntax error!");
            socket = null;
            e.printStackTrace();
            return;
        }
        if (socket == null) return;

        // Cache discord users into the database
        socket.on("discord:delinkrequest", args -> {
            dashboard.getLogger().info("Got delink request");
            DiscordCacheInfo json = gson.fromJson(args[0].toString(), DiscordCacheInfo.class);
            dashboard.getSqlUtil().selectAndRemoveDiscord(json);
        });
        socket.on(Socket.EVENT_CONNECT, args ->
                dashboard.getLogger().info(ChatColor.DARK_GREEN + "Discord link socket connected"))
                .on(Socket.EVENT_DISCONNECT, args ->
                        dashboard.getLogger().info(ChatColor.DARK_RED + "Discord link socket disconnected"));
        socket.connect();
    }

    public static void sendRemove(DiscordCacheInfo info) {
        if (socket == null) return;
        if (!socket.connected()) return;
        socket.emit("discord:remove", gson.toJson(info));
    }

    public static void sendLink(DiscordUserInfo info) {
        if (socket == null) return;
        if (!socket.connected()) return;
        socket.emit("discord:link", gson.toJson(info));
    }

    public static void sendNewlink(DiscordCacheInfo info) {
        if (socket == null) return;
        if (!socket.connected()) return;
        socket.emit("discord:newlink", gson.toJson(info));
    }

    public static void sendUpdate(DiscordCacheInfo info) {
        if (socket == null) return;
        if (!socket.connected()) return;
        socket.emit("discord:update", gson.toJson(info));
    }
}

