package com.palacemc.dashboard.forums.db;

import com.palacemc.dashboard.Dashboard;

import java.sql.SQLException;

/**
 * Created by Marc on 12/12/16.
 */
public abstract class AsyncDbStatement {
    protected String query;
    private boolean done = false;

    public AsyncDbStatement() {
        this.queue(null);
    }

    public AsyncDbStatement(String query) {
        this.queue(query);
    }

    private void queue(String query) {
        this.query = query;
        AsyncDbQueue.queue(this);
    }

    protected abstract void run(DbStatement var1) throws SQLException;

    public void onError(SQLException e) {
        Dashboard.getLogger().warn("Exception in AsyncDbStatement" + this.query);
        e.printStackTrace();
    }

    public void process(DbStatement stm) throws SQLException {
        synchronized (this) {
            if (!this.done) {
                if (this.query != null) {
                    stm.query(this.query);
                }

                this.run(stm);
                this.done = true;
            }

        }
    }
}
