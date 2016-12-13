package com.palacemc.dashboard.forums.db;

import com.palacemc.dashboard.Dashboard;

import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Marc on 12/12/16.
 */
public class AsyncDbQueue implements Runnable {
    private static final Queue<AsyncDbStatement> queue = new ConcurrentLinkedQueue<>();
    private static final Lock lock = new ReentrantLock();

    public void run() {
        processQueue();
    }

    public static void processQueue() {
        if (!queue.isEmpty() && lock.tryLock()) {
            AsyncDbStatement stm = null;

            DbStatement dbStatement;
            try {
                dbStatement = new DbStatement();
            } catch (Exception var4) {
                lock.unlock();
                Dashboard.getLogger().warn("Exception getting DbStatement in AsyncDbQueue");
                var4.printStackTrace();
                return;
            }

            while ((stm = queue.poll()) != null) {
                try {
                    if (dbStatement.isClosed()) {
                        dbStatement = new DbStatement();
                    }

                    stm.process(dbStatement);
                } catch (SQLException var3) {
                    stm.onError(var3);
                }
            }

            dbStatement.close();
            lock.unlock();
        }
    }

    public static boolean queue(AsyncDbStatement stm) {
        return queue.offer(stm);
    }
}
