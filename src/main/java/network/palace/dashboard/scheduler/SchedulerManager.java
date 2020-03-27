package network.palace.dashboard.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import network.palace.dashboard.Launcher;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

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
        } catch (RejectedExecutionException e) {
//            e.printStackTrace();
            LogRecord rec = new LogRecord(Level.SEVERE, "Error scheduling async task in ScheduleManager");
            rec.setThrown(e);
            Launcher.getDashboard().getLogger().log(rec);
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