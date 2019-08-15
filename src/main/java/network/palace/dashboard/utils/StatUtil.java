package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Marc on 9/28/16
 */
public class StatUtil {
    private final String url, username, password, database;

    private int totalPackets = 0; // used to calculate packets per second
    private long lastPacketReset = 0;
    private int totalLogins = 0;

    public StatUtil() throws SQLException, IOException {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getLogger().info("Initializing StatUtil...");
        String url = "";
        String username = "";
        String password = "";
        String database = "";
        try (BufferedReader br = new BufferedReader(new FileReader("stats.txt"))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("url:")) {
                    url = line.split("url:")[1];
                }
                if (line.startsWith("username:")) {
                    username = line.split("username:")[1];
                }
                if (line.startsWith("password:")) {
                    password = line.split("password:")[1];
                }
                if (line.startsWith("database:")) {
                    database = line.split("database:")[1];
                }
                line = br.readLine();
            }
        }
        this.url = url;
        this.username = username;
        this.password = password;
        this.database = database;

        int production = dashboard.isTestNetwork() ? 0 : 1;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();

                float packets = totalPackets;
                totalPackets = 0;
                long packetReset = lastPacketReset;
                lastPacketReset = time;

                int logins = totalLogins;
                totalLogins = 0;
                List<Point> points = new ArrayList<>();

                points.add(Point.measurement("logins").addField("count", logins).tag("production", String.valueOf(production)).build());
                if (packetReset != 0)
                    points.add(Point.measurement("dashboard_pps").addField("pps", packets / (time - packetReset)).tag("production", String.valueOf(production)).build());

                int totalPlayerCount = dashboard.getOnlinePlayers().size();
                points.add(Point.measurement("player_count")
                        .addField("count", totalPlayerCount)
                        .tag("production", String.valueOf(production)).build());
                HashMap<UUID, Integer> counts = new HashMap<>();
                Launcher.getDashboard().getOnlinePlayers().forEach(p -> {
                    UUID bid = p.getBungeeID();
                    counts.put(bid, counts.getOrDefault(bid, 0) + 1);
                });
                Dashboard.getChannels(PacketConnectionType.ConnectionType.BUNGEECORD).forEach(c -> points.add(Point.measurement("proxies")
                        .tag("name", c.getServerName()).addField("count",
                                counts.getOrDefault(c.getBungeeID(), 0)).build()));
                dashboard.getSchedulerManager().runAsync(() -> logDataPoint(points));
            }
        }, 10000, 10000);
        dashboard.getLogger().info("StatUtil is ready to go!");
    }

    public void packet() {
        totalPackets++;
    }

    public void newLogin() {
        totalLogins++;
    }

    public void logDataPoint(Point... points) {
        logDataPoint(new ArrayList<>(Arrays.asList(points)));
    }

    public void logDataPoint(List<Point> points) {
        InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
        influxDB.setDatabase(database);
        influxDB.setRetentionPolicy("autogen");
        try {
            BatchPoints batchPoints = BatchPoints.database("palace").tag("async", "true")
                    .consistency(InfluxDB.ConsistencyLevel.ALL).build();
            points.forEach(batchPoints::point);
            influxDB.write(batchPoints);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}