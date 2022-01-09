package com.jay.dove.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 19:48
 */
public class NamedThreadFactory implements ThreadFactory {

    private String name;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private boolean isDaemon;

    public NamedThreadFactory(String name) {
        this.name = name;
        this.isDaemon = true;
    }

    public NamedThreadFactory(String name, boolean isDaemon) {
        this.name = name;
        this.isDaemon = isDaemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, name + threadNumber.getAndIncrement());
        // set thread to daemon
        thread.setDaemon(isDaemon);
        // set thread priority to normal
        if(thread.getPriority() != Thread.NORM_PRIORITY){
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
