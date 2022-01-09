package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
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

    private int limit;
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

    public ConnectionPool(InetSocketAddress address, ConnectionFactory connectionFactory, ConnectionSelectStrategy selectStrategy, int limit) {
        this.connectionFactory = connectionFactory;
        this.selectStrategy = selectStrategy;
        this.limit = limit;
        connections = new CopyOnWriteArrayList<Connection>();
        this.address = address;
    }

    public void add(Connection connection){
        connections.addIfAbsent(connection);
    }

    public Connection getConnection(){
        ArrayList<Connection> candidates = new ArrayList<>(this.connections);
        return selectStrategy.select(candidates);
    }

    public Connection createAndGetConnection(int timeout) throws Exception {
        log.info("creating new connection");
        Connection connection = connectionFactory.create(address, timeout);
        connections.addIfAbsent(connection);
        return connection;
    }

    /**
     * can other thread create connection.
     * only when pool is not full and no warm-up task is running.
     * @return boolean
     */
    public boolean canCreateConnection(){
        return connections.size() != limit && !warmingUp.get();
    }

    /**
     * warm up a connection pool using an executor
     * @param executor {@link ExecutorService}
     * @param timeout connect timeout
     */
    public void warmUpConnectionPool(ExecutorService executor, int timeout) {
        Runnable warmUpTask = ()->{
            long startTime = System.currentTimeMillis();
            int retryTimes = 0;
            // max retry times
            int maxRetryTimes = Configs.connectMaxRetry();
            for (int cur = connections.size(); cur < limit; cur++){
                try{
                    // create one connection
                    Connection connection = connectionFactory.create(address, timeout);
                    // save to connection pool
                    connections.addIfAbsent(connection);
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
