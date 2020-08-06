package network.palace.dashboard.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import network.palace.dashboard.handlers.ChatMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Marc on 7/14/16
 */
public class SqlUtil {
    private HikariDataSource mainDatabase;

    public SqlUtil() throws SQLException, IOException {
        String address = "";
        String database = "";
        String username = "";
        String password = "";
        try (BufferedReader br = new BufferedReader(new FileReader("sql.txt"))) {
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

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + address + ":3306/" + database);
        config.setUsername(username);
        config.setPassword(password);

        // See: https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        mainDatabase = new HikariDataSource(config);
    }

    public Optional<Connection> getConnection() {
        try {
            return Optional.of(mainDatabase.getConnection());
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    public void stop() {
        mainDatabase.close();
    }

    public void logChat(List<ChatMessage> messages) throws Exception {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            throw new SQLException("Could not establish an SQL connection");
        }
        StringBuilder values = new StringBuilder();
        long time = System.currentTimeMillis();
        for (int i = 0; i < messages.size(); i++) {
            values.append("(?,?,?)");
            if (i < (messages.size() - 1)) {
                values.append(",");
            }
        }
        PreparedStatement sql = connection.get().prepareStatement("INSERT INTO chat (uuid,message,time) VALUES " + values);
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            sql.setString((i * 3) + 1, msg.getUuid().toString());
            sql.setString((i * 3) + 2, msg.getMessage());
            sql.setLong((i * 3) + 3, msg.getTime());
        }
        sql.execute();
        sql.close();
    }
}