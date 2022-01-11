package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
import com.jay.dove.transport.Url;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
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
    private final InetSocketAddress address;
    /**
     * connections list
     */
    private final CopyOnWriteArrayList<Connection> connections;
    /**
     * select strategy
     */
    private final ConnectionSelectStrategy selectStrategy;

    private final ConnectionFactory connectionFactory;

    /**
     * whether this pool is warming up.
     * If this pool is warming up, other threads can't add new Connection into it.
     */
    private final AtomicBoolean warmingUp = new AtomicBoolean(false);

    public ConnectionPool(Url url, ConnectionFactory connectionFactory, ConnectionSelectStrategy selectStrategy){
        this.address = new InetSocketAddress(url.getIp(), url.getPort());
        this.connectionFactory = connectionFactory;
        this.selectStrategy = selectStrategy;
        this.connections = new CopyOnWriteArrayList<>();
    }

    public ConnectionPool(InetSocketAddress address, ConnectionFactory connectionFactory, ConnectionSelectStrategy selectStrategy) {
        this.connectionFactory = connectionFactory;
        this.selectStrategy = selectStrategy;
        connections = new CopyOnWriteArrayList<>();
        this.address = address;
    }

    public void add(Connection connection){
        this.connections.add(connection);
    }

    public Connection getConnection(){
        ArrayList<Connection> candidates = new ArrayList<>(this.connections);
        return selectStrategy.select(candidates);
    }

    public Connection createAndGetConnection(int timeout) throws Exception {
        Connection connection = connectionFactory.create(address, timeout);
        connections.add(connection);
        return connection;
    }

    /**
     * heal a connection pool using an executor
     * @param executor {@link ExecutorService}
     * @param timeout connect timeout
     */
    public void healConnectionPool(ExecutorService executor, int expectedCount, int timeout)throws Exception {
        markAsyncWarmUpStart();
        Runnable warmUpTask = ()->{
            long startTime = System.currentTimeMillis();
            int retryTimes = 0;
            // max retry times
            int maxRetryTimes = Configs.connectMaxRetry();
            for (int cur = connections.size(); cur < expectedCount; cur++){
                try{
                    Connection connection = connectionFactory.create(address, timeout);
                    connections.add(connection);
                }catch (Exception e){
                    log.error("connection warm up failed", e);
                    break;
                }
            }
            log.info("connection pool warm up for {} finished, time used: {}ms", address, (System.currentTimeMillis() - startTime));
            // finish warming up
            markAsyncWarmUpDone();
        };
        executor.submit(warmUpTask);
    }

    private void markAsyncWarmUpStart(){
        for(;;){
            if(warmingUp.compareAndSet(false, true)){
                break;
            }
        }
    }

    private void markAsyncWarmUpDone(){
        warmingUp.set(false);
    }

}
