package com.jay.dove.transport.connection;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final ConcurrentHashMap<InetSocketAddress, ConnectionPool> CONNECTION_MAP = new ConcurrentHashMap<>(256);

    public static final int DEFAULT_CONNECTION_COUNT = 10;

    public Connection getConnection(InetSocketAddress address){
        // get the connection pool of this address. Create a new pool if not exist.
        ConnectionPool connectionPool = CONNECTION_MAP.computeIfAbsent(address, key -> {
            return createConnections(key, DEFAULT_CONNECTION_COUNT, false);
        });
        // select a connection from pool.
        return connectionPool.getConnection();
    }

    /**
     * create connection pool for target address.
     * @param address target Address
     * @param count the count of connections to create
     * @param warmup true, if you want the background thread to establish all connections now.
     */
    public ConnectionPool createConnections(InetSocketAddress address, int count, boolean warmup){
        return null;
    }
}
