package com.palacemc.dashboard.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Marc on 7/15/16
 */
public class SchedulerManager {
    private final ExecutorService executor;
    private final BroadcastClock broadcastClock;
    private final Timer broadcastTimer;

    public SchedulerManager() {
        this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Dashboard Pool Thread #%1$d")
                .setThreadFactory(new GroupedThreadFactory("Dashboard")).build());

        broadcastClock = new BroadcastClock();
        broadcastTimer = new Timer();
        broadcastTimer.schedule(broadcastClock, 10000, 300000);
    }

    public void runAsync(Runnable runnable) {
        executor.execute(runnable);
    }

    public void stop() {
        broadcastTimer.cancel();

        List<Runnable> tasks = executor.shutdownNow();

        for (Runnable runnable : tasks) {
            runnable.run();
        }
    }

    public BroadcastClock getBroadcastClock() {
        return broadcastClock;
    }
}