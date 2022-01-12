package com.jay.dove.common;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 *  Abstract LifeCycle.
 *  This class does common life-cycle jobs.
 * </p>
 *
 * @author Jay
 * @date 2022/01/12 10:36
 */
public abstract class AbstractLifeCycle implements LifeCycle{

    private final AtomicBoolean started = new AtomicBoolean(false);

    @Override
    public void startup() {
        if(started.compareAndSet(false, true)){
            return;
        }
        throw new IllegalStateException("component started already");
    }

    @Override
    public void shutdown() {
        if(started.compareAndSet(true, false)){
            return;
        }
        throw new IllegalStateException("component has been closed");
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    public void ensureStarted(){
        if(!isStarted()){
            throw new IllegalStateException("component not started");
        }
    }
}
