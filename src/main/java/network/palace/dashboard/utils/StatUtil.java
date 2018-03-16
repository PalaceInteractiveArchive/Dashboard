package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Marc on 9/28/16
 */
public class StatUtil {
    private int playerCount = 0;

    public StatUtil() {
        Dashboard dashboard = Launcher.getDashboard();
        if (dashboard.isTestNetwork()) {
            return;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int count;
                try {
                    count = dashboard.getOnlinePlayers().size();
                } catch (Exception e) {
                    count = 0;
                }
                if (count != playerCount) {
                    playerCount = count;
                }
                dashboard.getSchedulerManager().runAsync(() -> setValue(playerCount));
            }
        }, 60000, 60000);
    }

    private void setValue(int value) {
        Dashboard dashboard = Launcher.getDashboard();
        /*
        Optional<Connection> optConnection = dashboard.getMongoHandler().getConnection();
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
        }*/
    }
}