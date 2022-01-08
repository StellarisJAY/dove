package com.jay.dove.transport.connection;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 *  Connection pool, holds all connections to a same remote address.
 *  Contains a select strategy.
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 20:22
 */
public class ConnectionPool {
    /**
     * connections list
     */
    private final CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<>();
    /**
     * select strategy
     */
    private final ConnectionSelectStrategy selectStrategy;

    public ConnectionPool(ConnectionSelectStrategy selectStrategy) {
        this.selectStrategy = selectStrategy;
    }

    public void add(Connection connection){
        connections.addIfAbsent(connection);
    }

    public Connection getConnection(){
        ArrayList<Connection> candidates = new ArrayList<>(this.connections);
        return selectStrategy.select(candidates);
    }


}
