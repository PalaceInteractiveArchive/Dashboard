package network.palace.dashboard.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import network.palace.dashboard.Launcher;
import network.palace.dashboard.utils.ErrorUtil;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

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
        if (runnable == null)
            return;
        try {
            executor.submit(runnable);
            Launcher.getDashboard().getLogger().warn("SUCCESSFULLY SUBMITTED RUNNABLE");
        } catch (RejectedExecutionException e) {
            ErrorUtil.logError("Error scheduling async task in SchedulerManager", e);
        }
    }

    public void stop() {
        broadcastTimer.cancel();
        List<Runnable> tasks = executor.shutdownNow();
        for (Runnable r : tasks) {
            r.run();
        }
    }

    public BroadcastClock getBroadcastClock() {
        return broadcastClock;
    }
}