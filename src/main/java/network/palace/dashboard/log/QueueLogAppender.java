package network.palace.dashboard.log;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Plugin(name = "Queue", category = "Core", elementType = "appender", printObject = true)
public class QueueLogAppender extends AbstractAppender {
    private static final int MAX_CAPACITY = 250;
    private static final Map<String, BlockingQueue<String>> QUEUES = new HashMap<>();
    private static final ReadWriteLock QUEUE_LOCK = new ReentrantReadWriteLock();

    private final BlockingQueue<String> queue;

    public QueueLogAppender(String paramString, Filter paramFilter, Layout<? extends Serializable> paramLayout, boolean paramBoolean, BlockingQueue<String> paramBlockingQueue) {
        super(paramString, paramFilter, paramLayout, paramBoolean, Property.EMPTY_ARRAY);
        this.queue = paramBlockingQueue;
    }


    public void append(LogEvent paramLogEvent) {
        if (getLayout() == null) return;
        if (this.queue.size() >= MAX_CAPACITY) this.queue.clear();
        try {
            this.queue.add(getLayout().toSerializable(paramLogEvent).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PluginFactory
    public static QueueLogAppender createAppender(@PluginAttribute("name") String paramString1,
                                                  @PluginAttribute("ignoreExceptions") String paramString2,
                                                  @PluginElement("Layout") Layout<? extends Serializable> paramLayout,
                                                  @PluginElement("Filters") Filter paramFilter,
                                                  @PluginAttribute("target") String paramString3) {
        PatternLayout patternLayout = null;
        boolean bool = Boolean.parseBoolean(paramString2);

        if (paramString1 == null) {
            LOGGER.error("No name provided for QueueLogAppender");
            return null;
        }

        if (paramString3 == null) {
            paramString3 = paramString1;
        }

        QUEUE_LOCK.writeLock().lock();
        BlockingQueue<String> blockingQueue = QUEUES.get(paramString3);
        if (blockingQueue == null) {
            blockingQueue = new LinkedBlockingQueue<>();
            QUEUES.put(paramString3, blockingQueue);
        }
        QUEUE_LOCK.writeLock().unlock();

        if (paramLayout == null) {
            patternLayout = PatternLayout.newBuilder().build();
        }

        return new QueueLogAppender(paramString1, paramFilter, patternLayout, bool, blockingQueue);
    }

    public static String getNextLogEvent(String paramString) {
        QUEUE_LOCK.readLock().lock();
        BlockingQueue<String> blockingQueue = QUEUES.get(paramString);
        QUEUE_LOCK.readLock().unlock();

        if (blockingQueue != null) {
            try {
                return blockingQueue.take();
            } catch (InterruptedException interruptedException) {
            }
        }

        return null;
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
        return PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss}] [%t/%level]: %msg%n").build();
    }
}
