package com.palacemc.dashboard.utils;

import com.palacemc.dashboard.Launcher;

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
        if (Launcher.getDashboard().isTestNetwork()) {
            return;
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int count = Launcher.getDashboard().getOnlinePlayers().size();
                if (count != playerCount) {
                    playerCount = count;
                }
                Launcher.getDashboard().getSchedulerManager().runAsync(() -> setValue(playerCount));
            }
        }, 0, 60000);
    }

    private void setValue(int value) {
        try (Connection connection = Launcher.getDashboard().getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO stats (time, type, value) VALUES ('" +
                    (System.currentTimeMillis() / 1000) + "','count','" + value + "')");
            sql.execute();
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}