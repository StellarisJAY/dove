package com.jay.dove.transport.connection;

import com.jay.dove.config.Configs;
import com.jay.dove.transport.connection.strategy.RandomSelectStrategy;
import com.jay.dove.util.NamedThreadFactory;
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
            return createConnectionPool(key, DEFAULT_CONNECTION_COUNT, 1);
        });
        // now we have the connection pool, select a connection from it.
        return connectionPool.getConnection();
    }

    /**
     * create connection pool for target address.
     * @param address target Address
     */
    public ConnectionPool createConnectionPool(InetSocketAddress address, int expectedCount, int syncCreate) {
        int timeout = Configs.connectTimeout();
        // create a pool instance
        ConnectionPool connectionPool = new ConnectionPool(address, connectionFactory, new RandomSelectStrategy());
        try {
            // async heal connection pool to expected count
            connectionPool.healConnectionPool(asyncConnectExecutor, expectedCount, syncCreate, timeout);
        } catch (Exception e) {
            log.error("connection pool creation error", e);
        }
        return connectionPool;
    }
}
