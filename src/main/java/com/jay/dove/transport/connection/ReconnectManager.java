package com.jay.dove.transport.connection;

import com.jay.dove.common.AbstractLifeCycle;
import com.jay.dove.transport.Url;
import com.jay.dove.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>
 *  Reconnect Manager.
 *  This class is used to do re-connect tasks automatically.
 *  It only has one worker thread to do re-connect in a fixed rate.
 * </p>
 *
 * @author Jay
 * @date 2022/01/12 10:31
 */
@Slf4j
public class ReconnectManager extends AbstractLifeCycle implements Reconnector {

    /**
     * blocking queue of reconnect tasks
     */
    private final LinkedBlockingQueue<ReconnectTask> tasks;
    /**
     * canceled reconnect tasks
     */
    private final List<Url> canceled;
    /**
     * {@link ConnectionManager}
     */
    private final ConnectionManager connectionManager;

    /**
     * reconnect thread.
     */
    private Thread reconnectThread;
    /**
     * fixed reconnect period
     */
    public static final long RECONNECT_INTERVAL = 1000;

    public ReconnectManager(ConnectionManager connectionManager) {
        this.canceled = new ArrayList<>();
        this.connectionManager = connectionManager;
        this.tasks = new LinkedBlockingQueue<>();
    }

    @Override
    public void startup() {
        synchronized (this){
            if(!isStarted()){
                super.startup();
                // create thread, set to daemon
                this.reconnectThread = NamedThreadFactory.createThread(null, "reconnect-thread", true);
                this.reconnectThread.start();
            }
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        // stop reconnect thread
        this.reconnectThread.interrupt();
        // clear tasks
        this.tasks.clear();
        this.canceled.clear();
    }

    @Override
    public void reconnect(Url url) {
        ensureStarted();
        tasks.add(new ReconnectTask(url));
    }

    @Override
    public void disableReconnect(Url url) {
        ensureStarted();
        canceled.add(url);
    }

    @Override
    public void enableReconnect(Url url) {
        ensureStarted();
        canceled.remove(url);
    }

    class ReconnectThreadTask implements Runnable{

        @Override
        public void run() {
            // time used by the last reconnect task
            long lastReconnectTime = RECONNECT_INTERVAL;
            long start = -1;
            while(isStarted()){
                ReconnectTask task = null;
                try{
                    // last reconnect ran too fast
                    if(lastReconnectTime < RECONNECT_INTERVAL){
                        Thread.sleep(RECONNECT_INTERVAL);
                    }
                    // take a re-connect task
                    try{
                        task = tasks.take();
                    }catch (InterruptedException e){
                        log.warn("reconnect task interrupted");
                    }
                    start = System.currentTimeMillis();
                    // check if the task is canceled
                    if(task != null && !canceled.contains(task.url)){
                        // run tasks
                        task.run();
                    }
                    lastReconnectTime = System.currentTimeMillis() - start;
                }catch (Exception e){
                    // catch exceptions from task.run()
                    log.error("reconnect task error ", e);
                    lastReconnectTime = System.currentTimeMillis() - start;
                    tasks.add(task);
                }
            }
        }
    }

    /**
     * reconnect task
     * This runnable uses ConnectionManager to heal a connection's pool
     */
    private final class ReconnectTask implements Runnable{

        private final Url url;

        public ReconnectTask(Url url) {
            this.url = url;
        }
        @Override
        public void run() {
            try {
                // use connectionManager to heal connection pool
                connectionManager.createConnectionPoolAndHealIfNeeded(url);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }
}
