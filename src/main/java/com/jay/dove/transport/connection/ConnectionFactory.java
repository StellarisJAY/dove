package com.jay.dove.transport.connection;

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
     * @param ip target ip
     * @param port target port
     * @param timeout connect timeout milleSeconds
     * @return {@link Connection}
     * @throws Exception exceptions {@link java.net.ConnectException}
     */
    Connection create(String ip, int port, int timeout) throws Exception;
}
