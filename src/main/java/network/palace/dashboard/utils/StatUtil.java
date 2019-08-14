package network.palace.dashboard.utils;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;

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
    private int playerCount;

    public StatUtil() throws SQLException, IOException {
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

        Dashboard dashboard = Launcher.getDashboard();
        String production = String.valueOf(dashboard.isTestNetwork() ? 0 : 1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    playerCount = dashboard.getOnlinePlayers().size();
                } catch (Exception e) {
                    playerCount = 0;
                }
                dashboard.getSchedulerManager().runAsync(() -> {
                    HashMap<String, String> values = new HashMap<>();
                    values.put("count", String.valueOf(playerCount));
                    values.put("time", String.valueOf(System.currentTimeMillis() / 1000));
                    values.put("production", production);
                    insertLogStatistic("player_count", values);
                });
            }
        }, 10000, 60000);
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

    public void insertLogStatistic(String tableName, HashMap<String, String> values) {
        StringBuilder columnNames = new StringBuilder();
        List<String> keySet = new ArrayList<>(values.keySet());
        for (int i = 0; i < values.size(); i++) {
            columnNames.append(keySet.get(i));
            if (i <= (values.size() - 1)) {
                columnNames.append(", ");
            }
        }

        String valueString = "";
        for (int i = 0; i < values.size(); i++) {
            columnNames.append("?");
            if (i <= (values.size() - 1)) {
                columnNames.append(", ");
            }
        }

        Optional<Connection> optConnection = getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO " + tableName +
                    " (" + columnNames.toString() + ") VALUES (" + valueString + ")");
            int i = 1;
            for (String s : values.values()) {
                sql.setString(i++, s);
            }
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            ErrorUtil.logError("Error in StatUtil with logStatistic method", e);
        }
    }
}