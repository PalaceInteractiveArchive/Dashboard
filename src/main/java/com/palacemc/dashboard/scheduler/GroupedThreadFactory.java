package com.palacemc.dashboard.scheduler;

import java.util.concurrent.ThreadFactory;

/**
 * Created by Marc on 7/15/16
 */
public class GroupedThreadFactory implements ThreadFactory {
    private final ThreadGroup group;

    public static class BungeeGroup extends ThreadGroup {
        private BungeeGroup(String name) {
            super(name);
        }
    }

    public GroupedThreadFactory(String name) {
        this.group = new BungeeGroup(name);
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(group, r);
    }
}