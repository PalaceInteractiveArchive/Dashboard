package network.palace.dashboard.forums.db;

import network.palace.dashboard.Dashboard;
import network.palace.dashboard.Launcher;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by Marc on 12/12/16.
 */
public class DbStatement implements AutoCloseable {
    Connection dbConn;
    PreparedStatement preparedStatement;
    ResultSet resultSet;
    public ResultSetMetaData resultSetMetaData;
    public String[] resultCols;
    public String query = "";
    public boolean isDirty = false;

    public DbStatement() throws SQLException {
        if (Database.connectionPool == null) {
            Launcher.getDashboard().getLogger().warn("No database connection");
        } else {
            this.dbConn = Database.connectionPool.getConnection();
        }
    }

    public DbStatement(Connection connection) throws SQLException {
        this.dbConn = connection;
    }

    public void startTransaction() throws SQLException {
        this.dbConn.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        this.dbConn.commit();
        this.isDirty = false;
        this.dbConn.setAutoCommit(true);
    }

    public void rollback() throws SQLException {
        this.dbConn.rollback();
        this.isDirty = false;
    }

    public DbStatement query(String query) throws SQLException {
        this.query = query;
        this.closeStatement();

        try {
            this.preparedStatement = this.dbConn.prepareStatement(query, 1);
            return this;
        } catch (SQLException var3) {
            this.close();
            throw var3;
        }
    }

    protected void prepareExecute(Object... params) throws SQLException {
        this.isDirty = true;
        this.closeResult();
        if (this.preparedStatement == null) {
            throw new IllegalStateException("Run Query first on statement before executing!");
        } else {
            for (int i = 0; i < params.length; ++i) {
                this.preparedStatement.setObject(i + 1, params[i]);
            }

        }
    }

    public int executeUpdate(Object... params) throws SQLException {
        try {
            this.prepareExecute(params);
            return this.preparedStatement.executeUpdate();
        } catch (SQLException var3) {
            this.close();
            throw var3;
        }
    }

    public DbStatement execute(Object... params) throws SQLException {
        try {
            this.prepareExecute(params);
            this.resultSet = this.preparedStatement.executeQuery();
            this.resultSetMetaData = this.resultSet.getMetaData();
            int e = this.resultSetMetaData.getColumnCount();
            this.resultCols = new String[e];

            for (int i = 1; i < e + 1; ++i) {
                this.resultCols[i - 1] = this.resultSetMetaData.getColumnLabel(i);
            }

            return this;
        } catch (SQLException var4) {
            this.close();
            throw var4;
        }
    }

    public Long getLastInsertId() throws SQLException {
        ResultSet genKeys = this.preparedStatement.getGeneratedKeys();
        Throwable var2 = null;

        Long result;
        try {
            if (genKeys != null) {
                result = null;
                if (genKeys.next()) {
                    result = genKeys.getLong(1);
                }
                return result;
            }

            result = null;
        } catch (Throwable var14) {
            var2 = var14;
            throw var14;
        } finally {
            if (genKeys != null) {
                if (var2 != null) {
                    try {
                        genKeys.close();
                    } catch (Throwable var13) {
                        var2.addSuppressed(var13);
                    }
                } else {
                    genKeys.close();
                }
            }

        }

        return result;
    }

    public ArrayList<DbRow> getResults() throws SQLException {
        if (this.resultSet == null) {
            return null;
        } else {
            ArrayList<DbRow> result = new ArrayList<>();

            DbRow row;
            while ((row = this.getNextRow()) != null) {
                result.add(row);
            }

            return result;
        }
    }

    public DbRow getNextRow() throws SQLException {
        if (this.resultSet == null) {
            return null;
        } else {
            ResultSet nextResultSet = this.getNextResultSet();
            if (nextResultSet == null) {
                return null;
            } else {
                DbRow row = new DbRow();
                String[] var3 = this.resultCols;
                int var4 = var3.length;
                for (String col : var3) {
                    row.put(col, nextResultSet.getObject(col));
                }
                return row;
            }
        }
    }

    public Object getFirstColumn() throws SQLException {
        ResultSet resultSet = this.getNextResultSet();
        return resultSet != null ? resultSet.getObject(1) : null;
    }

    protected ResultSet getNextResultSet() throws SQLException {
        if (this.resultSet != null && this.resultSet.next()) {
            return this.resultSet;
        } else {
            this.closeResult();
            return null;
        }
    }

    private void closeResult() throws SQLException {
        if (this.resultSet != null) {
            this.resultSet.close();
            this.resultSet = null;
        }

    }

    private void closeStatement() throws SQLException {
        if (this.preparedStatement != null) {
            this.preparedStatement.close();
            this.resultSet = null;
            this.preparedStatement = null;
        }

    }

    public void close() {
        Dashboard dashboard = Launcher.getDashboard();
        try {
            if (this.dbConn != null) {
                if (!this.dbConn.getAutoCommit() && this.isDirty) {
                    dashboard.getLogger().warn("Statement was not finalized: " + this.query);
                }

                this.dbConn.close();
            }

            this.preparedStatement = null;
            this.resultSet = null;
            this.dbConn = null;
        } catch (SQLException var2) {
            dashboard.getLogger().warn("Failed to close DB connection: " + this.query);
            var2.printStackTrace();
        }

    }

    public boolean isClosed() throws SQLException {
        return this.dbConn == null || this.dbConn.isClosed();
    }
}
