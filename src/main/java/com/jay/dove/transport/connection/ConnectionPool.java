package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
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
    private final int limit;
    /**
     * select strategy
     */
    private final ConnectionSelectStrategy selectStrategy;

    private final ConnectionFactory connectionFactory;


    private final Semaphore semaphore;
    /**
     * whether this pool is warming up.
     * If this pool is warming up, other threads can't add new Connection into it.
     */
    private final AtomicBoolean warmingUp = new AtomicBoolean(false);

    public ConnectionPool(InetSocketAddress address, ConnectionFactory connectionFactory, ConnectionSelectStrategy selectStrategy, int limit) {
        this.connectionFactory = connectionFactory;
        this.selectStrategy = selectStrategy;
        this.limit = limit;
        connections = new CopyOnWriteArrayList<>();
        this.address = address;
        semaphore = new Semaphore(limit);
    }

    public Connection getConnection(){
        ArrayList<Connection> candidates = new ArrayList<>(this.connections);
        return selectStrategy.select(candidates);
    }

    public Connection createAndGetConnection(int timeout) throws Exception {
        // check if it's able to create a connection now
        if(!warmingUp.get() && semaphore.tryAcquire()){
            // pool is warming up or full
            Connection connection = connectionFactory.create(address, timeout);
            connections.add(connection);
            return connection;
        }else{
            // wait for a connection
            while(true){
                if(this.connections.size() > 0){
                    return getConnection();
                }
            }
        }
    }

    /**
     * warm up a connection pool using an executor
     * @param executor {@link ExecutorService}
     * @param timeout connect timeout
     */
    public void warmUpConnectionPool(ExecutorService executor, int timeout) {
        warmingUp.set(true);
        Runnable warmUpTask = ()->{
            long startTime = System.currentTimeMillis();
            int retryTimes = 0;
            // max retry times
            int maxRetryTimes = Configs.connectMaxRetry();
            for (int cur = connections.size(); cur < limit; cur++){
                try{
                    // acquire a semaphore, check whether the pool is full
                    if(semaphore.tryAcquire()){
                        Connection connection = connectionFactory.create(address, timeout);
                        connections.add(connection);
                    }
                }catch (Exception e){
                    // retry and check retry times
                    cur --;
                    retryTimes++;
                    if(retryTimes == maxRetryTimes){
                        log.warn("connection pool warm up failed, target address {}", address);
                        break;
                    }
                }
            }
            log.info("connection pool warm up for {} finished, time used: {}ms", address, (System.currentTimeMillis() - startTime));
            // finish warming up
            warmingUp.set(false);
        };
        executor.submit(warmUpTask);
    }


}
