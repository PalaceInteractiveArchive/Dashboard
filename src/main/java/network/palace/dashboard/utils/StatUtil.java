package network.palace.dashboard.utils;

import network.palace.dashboard.Dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Marc on 9/28/16
 */
public class StatUtil {
    private int playerCount = 0;

    public StatUtil() {
        if (Dashboard.isTestNetwork()) {
            return;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int count = Dashboard.getOnlinePlayers().size();
                if (count != playerCount) {
                    playerCount = count;
                }
                Dashboard.schedulerManager.runAsync(() -> setValue(playerCount));
            }
        }, 0, 60000);
    }

    private void setValue(int value) {
        try (Connection connection = Dashboard.sqlUtil.getConnection()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO stats (time, type, value) VALUES ('" +
                    (System.currentTimeMillis() / 1000) + "','count','" + value + "')");
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}