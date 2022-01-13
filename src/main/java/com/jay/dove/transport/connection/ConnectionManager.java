package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
import com.jay.dove.transport.Url;
import com.jay.dove.transport.connection.strategy.RandomSelectStrategy;
import com.jay.dove.util.FutureTaskUtil;
import com.jay.dove.util.NamedThreadFactory;
import com.jay.dove.util.RunStateRecordedFutureTask;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * <p>
 *  Connection Manager.
 *  Manages connection pool of each address.
 * </p>
 *
 * @author Jay
 * @date 2022/01/08 15:43
 */
@Slf4j
public class ConnectionManager {

    private final ConcurrentHashMap<String, RunStateRecordedFutureTask<ConnectionPool>> connPoolTasks = new ConcurrentHashMap<>(256);

    private final ConcurrentHashMap<String, FutureTask<Integer>> healTasks = new ConcurrentHashMap<>(256);
    /**
     * connection factory, produces connections of the same protocol
     */
    private ConnectionFactory connectionFactory;


    /**
     * async connect executor.
     * Background threads used to create connections.
     */
    private final ExecutorService asyncConnectExecutor = new ThreadPoolExecutor(2, 2,
            0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory("async-connect-thread", true));


    public ConnectionManager(){

    }

    public ConnectionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        // init connection factory
        this.connectionFactory.init();
    }

    /**
     * remove a dead connection
     * @param connection connection
     */
    public void remove(Connection connection){
        String poolKey = connection.getPoolKey();
        // get connection pool
        RunStateRecordedFutureTask<ConnectionPool> task = connPoolTasks.get(poolKey);
        ConnectionPool pool;
        if(task == null || (pool = FutureTaskUtil.getFutureTaskResult(task)) == null){
            // connection pool absent
            log.warn("remove standalone connection {}", connection);
        }else{
            // remove connection in pool
            pool.remove(connection);
        }
        // close connection
        connection.close();
    }

    public void add(Connection connection){
        ConnectionPool pool = this.getConnectionPoolAndCreateIfAbsent(connection.getUrl());
        if(pool != null){
            pool.add(connection);
        }
    }

    /**
     * create a connection
     * @param url url {@link Url}
     * @return {@link Connection}
     * @throws Exception exceptions
     */
    public Connection createConnection(Url url) throws Exception {
        return this.connectionFactory.create(url, Configs.connectTimeout());
    }

    /**
     * get a connection from managed connection pool
     * @param url {@link Url}
     * @return {@link Connection}
     */
    public Connection getConnection(Url url) {
        // get connection pool creation task
        RunStateRecordedFutureTask<ConnectionPool> future = connPoolTasks.get(url.getPoolKey());
        // get pool from task
        ConnectionPool connectionPool = FutureTaskUtil.getFutureTaskResult(future);
        return connectionPool != null ? connectionPool.getConnection() : null;
    }

    /**
     * get a connection.
     * If connection pool absent, create a new connection pool
     * @param url {@link Url}
     * @return {@link Connection}
     */
    public Connection getAndCreateConnectionIfAbsent(Url url){
        ConnectionPool pool = getConnectionPoolAndCreateIfAbsent(url);
        return pool != null ? pool.getConnection() : null;
    }

    /**
     * get ConnectionPool and create if absent
     * @param url {@link Url} target url
     * @return {@link ConnectionPool}
     */
    public ConnectionPool getConnectionPoolAndCreateIfAbsent(Url url){
        if(connPoolTasks.get(url.getPoolKey()) == null){
            // no cached conn pool task
            RunStateRecordedFutureTask<ConnectionPool> task = new RunStateRecordedFutureTask<>(new CreatePoolCallable(url, 1));
            // try to put new task into cache
            if(connPoolTasks.putIfAbsent(url.getPoolKey(), task) == null){
                // successfully put a task into cache, run task
                task.run();
            }
        }
        // get cached task
        RunStateRecordedFutureTask<ConnectionPool> connTask = connPoolTasks.get(url.getPoolKey());
        // get task result
        return FutureTaskUtil.getFutureTaskResult(connTask);
    }

    /**
     * this method is mainly used to heal connection pool.
     * But it will also create the pool if absent.
     * @param url {@link Url}
     * @throws Exception exceptions from healing task
     */
    public void createConnectionPoolAndHealIfNeeded(Url url) throws Exception {
        // get or create a pool
        ConnectionPool pool = getConnectionPoolAndCreateIfAbsent(url);
        if (pool != null) {
            // heal pool
            this.healConnectionPool(pool, url);
        }else{
            log.warn("heal task failed, pool hasn't been created yet, url:{}", url);
        }
    }

    /**
     * heal connection pool async
     * @param pool {@link ConnectionPool}
     * @param url {@link Url}
     */
    public void healConnectionPool(ConnectionPool pool, Url url) throws Exception {
        int originalPoolSize = pool.size();
        String poolKey = url.getPoolKey();
        // check if there's an async warm up running & if the pool size is enough
        if(pool.isAsyncWarmUpDone() && originalPoolSize < url.getExpectedConnectionCount()){
            FutureTask<Integer> task = healTasks.get(poolKey);
            // check if there's a running heal task
            if(task == null){
                // create a new heal task
                task = new FutureTask<>(new HealPoolCallable(pool, url.getExpectedConnectionCount()));
                if(healTasks.putIfAbsent(poolKey, task) == null){
                    // successfully put into task cache, run heal task
                    task.run();
                }
            }
            try{
                // get healing result
                int sizeAfterHeal = task.get();
                log.info("connection pool {} healed, expected: {}, current size: {}, increment: {}", poolKey, url.getExpectedConnectionCount(), sizeAfterHeal, (sizeAfterHeal - originalPoolSize));
            } catch (ExecutionException | InterruptedException e) {
                log.error("heal connection pool failed, poolKey:  {}",poolKey, e);
                throw e;
            }
            healTasks.remove(poolKey);
        }
    }

    /**
     * create pool task callable
     */
    final class CreatePoolCallable implements Callable<ConnectionPool>{

        private final Url url;
        private final int syncCreateConnCount;

        public CreatePoolCallable(Url url, int syncCreateConnCount) {
            this.url = url;
            this.syncCreateConnCount = syncCreateConnCount;
        }

        @Override
        public ConnectionPool call() throws Exception {
            // create a pool, using Random Select Strategy temporarily
            ConnectionPool pool = new ConnectionPool(url, connectionFactory, new RandomSelectStrategy());
            int expectedPoolSize = url.getExpectedConnectionCount();
            int connTimeOut = Configs.connectTimeout();
            if(syncCreateConnCount > 0){
                // sync create some connections
                for (int i = 0; i < syncCreateConnCount; i++){
                    Connection connection = connectionFactory.create(url, connTimeOut);
                    pool.add(connection);
                }
            }
            // async heal connection pool
            pool.healConnectionPool(asyncConnectExecutor, expectedPoolSize, connTimeOut);
            return pool;
        }
    }

    /**
     * heal connection pool callable
     * This callable returns pool's size after healing
     */
    final class HealPoolCallable implements Callable<Integer>{
        private final ConnectionPool pool;
        private final int expectedCount;
        public HealPoolCallable(ConnectionPool pool, int expectedCount) {
            this.pool = pool;
            this.expectedCount = expectedCount;
        }

        @Override
        public Integer call() {
            try{
                // call connection pool's heal()
                pool.healConnectionPool(asyncConnectExecutor, expectedCount, Configs.connectTimeout());
            }catch (Exception e){
                throw new RuntimeException(e);
            }
            // return pool size after healing
            return pool.size();
        }
    }
}
