package com.palacemc.dashboard.handlers;

import com.palacemc.dashboard.Dashboard;
import com.palacemc.dashboard.Launcher;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Created by Marc on 5/29/16
 */
public class Server {
    @Getter private UUID uuid = UUID.randomUUID();
    @Getter private String name;
    @Getter private String address;
    @Getter private int port;
    @Getter private boolean park;
    @Getter private int count;
    @Getter private String serverType;
    @Getter @Setter private boolean online = false;

    private Dashboard dashboard = Launcher.getDashboard();

    public Server(String name, String address, int port, boolean park, int count, String serverType) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.park = park;
        this.count = count;
        this.serverType = serverType;
    }

    public void changeCount(int i) {
        this.count += i;
    }

    public void emptyServer() {
        Server s = null;
        for (Server server : dashboard.getServerUtil().getServers()) {
            if (server.getUuid().equals(getUuid())) {
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