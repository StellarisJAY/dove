package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
import com.jay.dove.transport.Url;
import com.jay.dove.transport.connection.strategy.RandomSelectStrategy;
import com.jay.dove.util.FutureTaskUtil;
import com.jay.dove.util.NamedThreadFactory;
import com.jay.dove.util.RunStateRecordedFutureTask;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
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
    /**
     * Connection map, address:Pool
     * ConcurrentHashMap, because we want all the operations to be Atomic
     */
    private final ConcurrentHashMap<InetSocketAddress, ConnectionPool> CONNECTION_MAP = new ConcurrentHashMap<>(256);

    private final ConcurrentHashMap<String, RunStateRecordedFutureTask<ConnectionPool>> connPoolTasks = new ConcurrentHashMap<>(256);
    /**
     * connection factory, produces connections of the same protocol
     */
    private final ConnectionFactory connectionFactory;


    /**
     * async connect executor.
     * Background threads used to create connections.
     */
    private final ExecutorService asyncConnectExecutor = new ThreadPoolExecutor(2, 2,
            0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory("async-connect-thread", true));

    /**
     * Default connections count of a pool
     */
    public static final int DEFAULT_CONNECTION_COUNT = 100;

    public ConnectionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        // init connection factory
        this.connectionFactory.init();
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
     * @throws ExecutionException execution exception
     * @throws InterruptedException Interrupted Exception
     */
    public Connection getConnection(Url url) throws ExecutionException, InterruptedException {
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
        ConnectionPool pool = getAndCreatePoolIfAbsent(url);
        return pool != null ? pool.getConnection() : null;
    }

    /**
     * get ConnectionPool and create if absent
     * @param url {@link Url} target url
     * @return {@link ConnectionPool}
     */
    public ConnectionPool getAndCreatePoolIfAbsent(Url url){
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

    class CreatePoolCallable implements Callable<ConnectionPool>{

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
}
