package network.palace.dashboard.handlers;

import network.palace.dashboard.Dashboard;

import java.util.UUID;

/**
 * Created by Marc on 5/29/16
 */
public class Server {
    private UUID uuid = UUID.randomUUID();
    private String name;
    private String address;
    private int port;
    private boolean park;
    private int count;
    private String serverType;
    private boolean online = false;

    public Server(String name, String address, int port, boolean park, int count, String serverType) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.park = park;
        this.count = count;
        this.serverType = serverType;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isPark() {
        return park;
    }

    public int getCount() {
        return count;
    }

    public String getServerType() {
        return serverType;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void changeCount(int i) {
        this.count += i;
    }

    public void emptyServer() {
        Server s = null;
        for (Server server : Dashboard.serverUtil.getServers()) {
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
        for (Player tp : Dashboard.getOnlinePlayers()) {
            if (tp.getServer().equals(getName())) {
                Dashboard.serverUtil.sendPlayer(tp, s.getName());
            }
        }
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}