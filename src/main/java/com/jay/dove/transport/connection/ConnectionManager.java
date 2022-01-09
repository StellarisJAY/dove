package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
import com.jay.dove.transport.connection.strategy.RandomSelectStrategy;
import com.jay.dove.util.NamedThreadFactory;

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
public class ConnectionManager {
    /**
     * Connection map, address:Pool
     * ConcurrentHashMap, because we want all the operations to be Atomic
     */
    private final ConcurrentHashMap<InetSocketAddress, ConnectionPool> CONNECTION_MAP = new ConcurrentHashMap<>(256);
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

    public Connection getConnection(InetSocketAddress address) throws Exception {
        /*
            get the connection pool of this address. Create a new pool if not exist.
            If more than one thread arrives here, we need to make sure only one thread calls createConnections
         */
        ConnectionPool connectionPool = CONNECTION_MAP.computeIfAbsent(address, key -> {
            return createConnections(key, DEFAULT_CONNECTION_COUNT, true);
        });
        // now we have the connection pool, now is to select a connection from it.
        return connectionPool.createAndGetConnection(Configs.connectTimeout());
    }

    /**
     * create connection pool for target address.
     * @param address target Address
     * @param count the count of connections to create
     * @param warmup true, if you want the background thread to establish all connections now.
     */
    public ConnectionPool createConnections(InetSocketAddress address, int count, boolean warmup){
        // create a pool instance
        ConnectionPool connectionPool = new ConnectionPool(address, connectionFactory, new RandomSelectStrategy(), count);
        if(warmup){
            // warm up pool using threadPoolExecutor
            connectionPool.warmUpConnectionPool(asyncConnectExecutor, Configs.connectTimeout());
        }
        return connectionPool;
    }
}
