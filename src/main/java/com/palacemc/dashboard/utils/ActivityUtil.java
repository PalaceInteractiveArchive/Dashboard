package com.palacemc.dashboard.utils;

import com.jolbox.bonecp.BoneCP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Marc on 8/22/16
 */
public class ActivityUtil {
    private BoneCP connectionPool;

    public ActivityUtil(BoneCP connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void logActivity(UUID uuid, String action, String description) {
        try (Connection connection = getConnection()) {
            PreparedStatement sql = connection.prepareStatement("INSERT INTO activity (uuid, action, description) VALUES (?,?,?)");
            sql.setString(1, uuid.toString());
            sql.setString(2, action);
            sql.setString(3, description);
            sql.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public void stop() {
        connectionPool.shutdown();
    }
}