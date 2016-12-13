package com.palacemc.dashboard.forums.db;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.palacemc.dashboard.Dashboard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Marc on 12/12/16.
 */
public final class Database {
    static BoneCP connectionPool = null;

    public static void initialize(String address, int port, String database, String username, String password) {
        try {
            BoneCPConfig config = new BoneCPConfig();
            config.setJdbcUrl("jdbc:mysql://" + address + ":3306/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setMinConnectionsPerPartition(5);
            config.setMaxConnectionsPerPartition(30);
            config.setPartitionCount(1);
            config.setIdleConnectionTestPeriod(600, TimeUnit.SECONDS);
            connectionPool = new BoneCP(config);
            /*HikariConfig ex = new HikariConfig();
            ex.setPoolName("MineSync-Connection-Pool");
            Bukkit.getLogger().info("Connecting to Database: " + address);
            ex.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            ex.addDataSourceProperty("serverName", address);
            ex.addDataSourceProperty("port", Integer.valueOf(port));
            ex.addDataSourceProperty("databaseName", database);
            ex.addDataSourceProperty("user", username);
            ex.addDataSourceProperty("password", password);
            ex.setMaximumPoolSize(10);
            ex.setConnectionTestQuery("SELECT 1");
            ex.setInitializationFailFast(true);
            ex.setMinimumIdle(3);
            ex.setMaximumPoolSize(5);
            ds = new HikariDataSource(ex);*/
        } catch (Exception var6) {
            connectionPool = null;
            Dashboard.getLogger().warn("No database connection, shutting down MineSync");
        }

    }

    public static DbStatement query(String query) throws SQLException {
        return (new DbStatement()).query(query);
    }

    public static List getResults(String query, Object... params) throws SQLException {
        DbStatement statement = query(query).execute(params);
        Throwable var3 = null;

        ArrayList<DbRow> var4;
        try {
            var4 = statement.getResults();
        } catch (Throwable var13) {
            var3 = var13;
            throw var13;
        } finally {
            if (statement != null) {
                if (var3 != null) {
                    try {
                        statement.close();
                    } catch (Throwable var12) {
                        var3.addSuppressed(var12);
                    }
                } else {
                    statement.close();
                }
            }

        }

        return var4;
    }

    public static DbRow getFirstRow(String query, Object... params) throws SQLException {
        DbStatement statement = query(query).execute(params);
        Throwable var3 = null;

        DbRow var4;
        try {
            var4 = statement.getNextRow();
        } catch (Throwable var13) {
            var3 = var13;
            throw var13;
        } finally {
            if (statement != null) {
                if (var3 != null) {
                    try {
                        statement.close();
                    } catch (Throwable var12) {
                        var3.addSuppressed(var12);
                    }
                } else {
                    statement.close();
                }
            }

        }

        return var4;
    }

    public static Object getFirstColumn(String query, Object... params) throws SQLException {
        DbStatement statement = query(query).execute(params);
        Throwable var3 = null;

        Object var4;
        try {
            var4 = statement.getFirstColumn();
        } catch (Throwable var13) {
            var3 = var13;
            throw var13;
        } finally {
            if (statement != null) {
                if (var3 != null) {
                    try {
                        statement.close();
                    } catch (Throwable var12) {
                        var3.addSuppressed(var12);
                    }
                } else {
                    statement.close();
                }
            }

        }

        return var4;
    }

    public static List getFirstColumnResults(String query, Object... params) throws SQLException {
        ArrayList<DbRow> dbRows = new ArrayList<>();
        DbStatement statement = query(query).execute(params);
        Throwable var5 = null;

        try {
            Object result;
            try {
                while ((result = statement.getFirstColumn()) != null) {
                    dbRows.add((DbRow) result);
                }
            } catch (Throwable var14) {
                var5 = var14;
                throw var14;
            }
        } finally {
            if (statement != null) {
                if (var5 != null) {
                    try {
                        statement.close();
                    } catch (Throwable var13) {
                        var5.addSuppressed(var13);
                    }
                } else {
                    statement.close();
                }
            }

        }

        return dbRows;
    }

    public static int executeUpdate(String query, Object... params) throws SQLException {
        DbStatement statement = query(query);
        Throwable var3 = null;

        int var4;
        try {
            var4 = statement.executeUpdate(params);
        } catch (Throwable var13) {
            var3 = var13;
            throw var13;
        } finally {
            if (statement != null) {
                if (var3 != null) {
                    try {
                        statement.close();
                    } catch (Throwable var12) {
                        var3.addSuppressed(var12);
                    }
                } else {
                    statement.close();
                }
            }

        }

        return var4;
    }

    public static void executeUpdateAsync(final String query, final Object... params) {
        AsyncDbStatement var10001 = new AsyncDbStatement(query) {
            public void run(DbStatement statement) throws SQLException {
                statement.executeUpdate(params);
            }
        };
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    public static void close() {
        connectionPool.close();
    }

    public static boolean isNull() {
        return connectionPool == null;
    }

    public void stop() {
        connectionPool.shutdown();
    }
}
