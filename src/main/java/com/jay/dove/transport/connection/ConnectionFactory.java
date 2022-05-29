package com.jay.dove.transport.connection;

import com.jay.dove.transport.Url;

import java.net.ConnectException;

/**
 * <p>
 *  Connection Factory interface
 * </p>
 *
 * @author Jay
 * @date 2022/01/09 11:24
 */
public interface ConnectionFactory {

    /**
     * init connection factory
     */
    void init();

    /**
     * create a connection
     * @param url target url {@link Url}
     * @param timeout timeout mille seconds
     * @return {@link Connection}
     * @throws IllegalArgumentException url or timeout illegal
     * @throws ConnectException connect exception
     */
    Connection create(Url url, int timeout) throws IllegalArgumentException, ConnectException;

    /**
     * shutdown connection factory's worker threads
     */
    void shutdown();

    void setConnectionManager(ConnectionManager connectionManager);
}
