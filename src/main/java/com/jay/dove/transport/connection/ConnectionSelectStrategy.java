package com.jay.dove.transport.connection;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 20:23
 */
public interface ConnectionSelectStrategy {
    /**
     * select connection
     * @param connections Connection List
     * @return {@link Connection}
     */
    Connection select(List<Connection> connections);
}
