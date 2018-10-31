package network.palace.dashboard.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.packets.arcade.GameState;

import java.util.UUID;

/**
 * Created by Marc on 5/29/16
 */
public class Server {
    private UUID uuid = UUID.randomUUID();
    @Getter private String name;
    @Getter private String address;
    @Getter private boolean park;
    @Getter @Setter private int count;
    @Getter @Setter private int gameMaxPlayers;
    @Getter private String serverType;
    @Getter @Setter private boolean online = false;
    @Getter @Setter private boolean inventory = false;
    @Getter @Setter private GameState gameState = GameState.LOBBY;
    @Getter @Setter private boolean gameNeedsUpdate = true;

    public Server(String name, String address, boolean park, int count, String serverType) {
        this.name = name;
        this.address = address;
        this.park = park;
        this.count = count;
        this.serverType = serverType;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void changeCount(int i) {
        this.count += i;
    }

    public void emptyServer() {
        Server s = null;
        Dashboard dashboard = Launcher.getDashboard();
        for (Server server : dashboard.getServerUtil().getServers()) {
            if (server.getUniqueId().equals(getUniqueId())) {
                continue;
            }
            if (server.getServerType().equalsIgnoreCase("hub")) {
                if (s == null) {
                    s = server;
                    continue;
                }
                if (server.getCount() < s.getCount()) {
                    s = server;
                }
            }
        }
        for (Player tp : dashboard.getOnlinePlayers()) {
            if (tp.getServer().equals(getName())) {
                dashboard.getServerUtil().sendPlayer(tp, s.getName());
            }
        }
    }
}