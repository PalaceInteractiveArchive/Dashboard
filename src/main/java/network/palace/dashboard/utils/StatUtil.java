package network.palace.dashboard.utils;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.handlers.Player;
import network.palace.dashboard.packets.dashboard.PacketConnectionType;
import network.palace.dashboard.server.DashboardSocketChannel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marc on 9/28/16
 */
public class StatUtil {
    private BoneCP connectionPool;
    private int totalLogins = 0;

    public StatUtil() throws SQLException, IOException {
        Dashboard dashboard = Launcher.getDashboard();
        dashboard.getLogger().info("Initializing StatUtil...");
        BoneCPConfig config = new BoneCPConfig();
        String address = "";
        String database = "";
        String username = "";
        String password = "";
        try (BufferedReader br = new BufferedReader(new FileReader("stats.txt"))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("address:")) {
                    address = line.split("address:")[1];
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
        config.setJdbcUrl("jdbc:mysql://" + address + ":3306/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMinConnectionsPerPartition(5);
        config.setMaxConnectionsPerPartition(25);
        config.setIdleMaxAge(100, TimeUnit.SECONDS);
        config.setMaxConnectionAge(300, TimeUnit.SECONDS);
        config.setPartitionCount(2);
        config.setIdleConnectionTestPeriod(300, TimeUnit.SECONDS);
        connectionPool = new BoneCP(config);
        dashboard.getLogger().info("StatUtil is ready to go!");

        int production = dashboard.isTestNetwork() ? 0 : 1;

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long time = System.currentTimeMillis() / 1000;
                int logins = totalLogins;
                totalLogins = 0;

                if (production == 1) {
                    HashMap<String, Object> totalLogins = new HashMap<>();
                    totalLogins.put("count", logins);
                    totalLogins.put("time", time);
                    insertLogStatistic("logins", totalLogins);
                }

                int totalPlayerCount = dashboard.getOnlinePlayers().size();

                List<HashMap<String, Object>> list = new ArrayList<>();

                List<DashboardSocketChannel> socketList = Dashboard.getChannels(PacketConnectionType.ConnectionType.BUNGEECORD);
                HashMap<UUID, Integer> counts = new HashMap<>();
                for (Player p : Launcher.getDashboard().getOnlinePlayers()) {
                    UUID bid = p.getBungeeID();
                    if (counts.containsKey(bid)) {
                        counts.put(bid, counts.get(bid) + 1);
                    } else {
                        counts.put(bid, 1);
                    }
                }
                for (DashboardSocketChannel c : socketList) {
                    int count = counts.getOrDefault(c.getBungeeID(), 0);
                    HashMap<String, Object> bungeeCountValues = new HashMap<>();
                    bungeeCountValues.put("name", c.getServerName());
                    bungeeCountValues.put("count", count);
                    bungeeCountValues.put("time", time);
                    list.add(bungeeCountValues);
                }

                dashboard.getSchedulerManager().runAsync(() -> {
                    list.forEach(map -> insertLogStatistic("proxies", map));

                    HashMap<String, Object> totalCountValues = new HashMap<>();
                    totalCountValues.put("count", totalPlayerCount);
                    totalCountValues.put("time", time);
                    totalCountValues.put("production", production);

                    insertLogStatistic("player_count", totalCountValues);
                });
            }
        }, 10000, 60000);
    }

    public void newLogin() {
        totalLogins++;
    }

    public Optional<Connection> getConnection() {
        try {
            return Optional.of(connectionPool.getConnection());
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public void stop() {
        connectionPool.shutdown();
    }

    public void insertLogStatistic(String tableName, HashMap<String, Object> values) {
        StringBuilder columnNames = new StringBuilder();
        List<String> keySet = new ArrayList<>(values.keySet());
        for (int i = 0; i < values.size(); i++) {
            columnNames.append(keySet.get(i));
            if (i < (values.size() - 1)) {
                columnNames.append(", ");
            }
        }

        StringBuilder valueString = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            valueString.append("?");
            if (i < (values.size() - 1)) {
                valueString.append(", ");
            }
        }

        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO " + tableName +
                    " (" + columnNames.toString() + ") VALUES (" + valueString.toString() + ")");
            int i = 1;
            for (Object o : values.values()) {
                if (o instanceof Integer) {
                    sql.setInt(i++, (Integer) o);
                } else if (o instanceof Double) {
                    sql.setDouble(i++, (Double) o);
                } else if (o instanceof Float) {
                    sql.setFloat(i++, (Float) o);
                } else if (o instanceof Short) {
                    sql.setShort(i++, (Short) o);
                } else if (o instanceof Boolean) {
                    sql.setBoolean(i++, (Boolean) o);
                } else if (o instanceof String) {
                    sql.setString(i++, (String) o);
                } else {
                    sql.setObject(i++, o);
                }
            }
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            ErrorUtil.logError("Error in StatUtil with logStatistic method", e);
        }
    }
}