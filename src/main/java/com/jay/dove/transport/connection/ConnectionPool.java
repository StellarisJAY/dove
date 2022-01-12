package com.jay.dove.transport.connection;

import com.jay.dove.transport.Url;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 *  Connection pool, holds all connections to a same remote address.
 *  Contains a select strategy.
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 20:22
 */
@Slf4j
public class ConnectionPool {
    /**
     * Connection pool's url
     */
    private final Url url;
    /**
     * connections list
     */
    private final CopyOnWriteArrayList<Connection> connections;
    /**
     * select strategy
     */
    private final ConnectionSelectStrategy selectStrategy;

    /**
     * Connection Factory used to create new Connections.
     */
    private final ConnectionFactory connectionFactory;

    /**
     * whether this pool is warming up.
     * If this pool is warming up, other threads can't add new Connection into it.
     */
    private final AtomicBoolean warmingUp = new AtomicBoolean(false);

    public ConnectionPool(Url url, ConnectionFactory connectionFactory, ConnectionSelectStrategy selectStrategy){
        this.connectionFactory = connectionFactory;
        this.selectStrategy = selectStrategy;
        this.connections = new CopyOnWriteArrayList<>();
        this.url = url;
    }

    public void add(Connection connection){
        this.connections.add(connection);
    }

    public void remove(Connection connection){
        this.connections.remove(connection);
    }

    /**
     * get a connection using select strategy
     * @return {@link Connection}
     */
    public Connection getConnection(){
        ArrayList<Connection> snapshot = new ArrayList<>(this.connections);
        // select a connection
        return selectStrategy.select(snapshot);
    }

    @Deprecated
    public Connection createAndGetConnection(int timeout) throws Exception {
        Connection connection = connectionFactory.create(url, timeout);
        connections.add(connection);
        return connection;
    }

    /**
     * heal a connection pool using an executor
     * @param executor {@link ExecutorService}
     * @param timeout connect timeout
     */
    public void healConnectionPool(ExecutorService executor, int expectedCount, int timeout) {
        markAsyncWarmUpStart();
        Runnable warmUpTask = ()->{
            long startTime = System.currentTimeMillis();
            for (int cur = connections.size(); cur < expectedCount; cur++){
                try{
                    // create a connection
                    Connection connection = connectionFactory.create(url, timeout);
                    // add to pool
                    connections.add(connection);
                }catch (Exception e){
                    log.error("connection warm up failed", e);
                    break;
                }
            }
            log.info("connection pool warm up for {} finished, poolSize: {}, expected: {} time used: {}ms", url, connections.size(), expectedCount, (System.currentTimeMillis() - startTime));
            // finish warming up
            markAsyncWarmUpDone();
        };
        // submit task to executor
        executor.submit(warmUpTask);
    }

    /**
     * mark Async warm up start.
     * This method uses CAS of AtomicBoolean, it only tries once.
     */
    private void markAsyncWarmUpStart(){
        if(warmingUp.compareAndSet(false, true)){
            return;
        }
        throw new IllegalStateException("warm up already started");
    }

    /**
     * mark Async warm up done.
     * This method uses CAS of AtomicBoolean, it only tries once.
     */
    private void markAsyncWarmUpDone(){
        if(warmingUp.compareAndSet(true, false)){
            return;
        }
        throw new IllegalStateException("warm up not started yet");
    }

    public boolean isAsyncWarmUpDone(){
        return warmingUp.get();
    }

    public int size(){
        return connections.size();
    }
}
