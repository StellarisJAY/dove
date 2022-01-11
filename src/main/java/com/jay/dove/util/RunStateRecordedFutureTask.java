package com.jay.dove.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/11 10:29
 */
public class RunStateRecordedFutureTask<V> extends FutureTask<V> {

    private final AtomicBoolean hasRun = new AtomicBoolean(false);

    public RunStateRecordedFutureTask(Callable<V> callable) {
        super(callable);
    }

    @Override
    public void run() {
        hasRun.set(true);
        super.run();
    }

    public V getResult() throws ExecutionException, InterruptedException {
        if(!hasRun.get()){
            throw new IllegalStateException("task has not run yet");
        }
        if(!isDone()){
            throw new IllegalStateException("task is not done yet");
        }
        return super.get();
    }
}
