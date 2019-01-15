package network.palace.dashboard.utils;

import com.goebl.david.Webb;
import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Marc on 9/28/16
 */
public class StatUtil {
    private int playerCount = 0;
    private static final String statsEndpoint = "https://api.palace.network/stats/player";
    private static final String apiKey = "aXDaexCS5NAS9mIOTRf3As9GS8exI7pMDYGtvS8N60Vl1ZbBcbBjPLEihADgNqmE";

    public StatUtil() {
        Dashboard dashboard = Launcher.getDashboard();
        if (dashboard.isTestNetwork()) {
            return;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    playerCount = dashboard.getOnlinePlayers().size();
                } catch (Exception e) {
                    playerCount = 0;
                }
                dashboard.getSchedulerManager().runAsync(() -> postToCachet(playerCount));
                dashboard.getSchedulerManager().runAsync(() -> setValue(playerCount));
            }
        }, 10000, 60000);
    }

    private void postToCachet(int value) {
        Webb webb = Webb.create();
        Launcher.getDashboard().getLogger().info("Sending request to API...");
        JSONObject response = webb.get(statsEndpoint + "/" + value + "?key=" + apiKey).asJsonObject().getBody();
        Launcher.getDashboard().getLogger().info("Request sent! Response: " + response.toString());
    }

    private void setValue(int value) {
        Dashboard dashboard = Launcher.getDashboard();
        Optional<Connection> optConnection = dashboard.getSqlUtil().getConnection();
        if (!optConnection.isPresent()) {
            ErrorUtil.logError("Unable to connect to mysql");
            return;
        }
        try (Connection connection = optConnection.get()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO stats (time, type, value) VALUES ('" +
                    (System.currentTimeMillis() / 1000) + "','count','" + value + "')");
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}