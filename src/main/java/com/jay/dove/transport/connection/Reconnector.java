package com.jay.dove.transport.connection;

import com.jay.dove.transport.Url;

/**
 * <p>
 *  Re-connector interface
 * </p>
 *
 * @author Jay
 * @date 2022/01/12 10:31
 */
public interface Reconnector {
    /**
     * re-connect to url
     * @param url {@link Url}
     */
    void reconnect(Url url);

    /**
     * disable reconnect task
     * @param url {@link Url}
     */
    void disableReconnect(Url url);

    /**
     * re-enable reconnect task
     * @param url {@link Url}
     */
    void enableReconnect(Url url);
}
