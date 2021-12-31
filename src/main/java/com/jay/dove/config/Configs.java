package com.jay.dove.config;

/**
 * <p>
 *  Configs of Dove
 * </p>
 *
 * @author Jay
 * @date 2021/12/31 10:12
 */
public class Configs {
    /**
     * TCP NO DELAY OPTIONS
     */
    public static final String TCP_NODELAY = "dove.tcp.nodelay";
    public static final String TCP_NODELAY_DEFAULT = "true";

    /**
     * TCP SO BACKLOG OPTION
     */
    public static final String TCP_SO_BACKLOG                        = "bolt.tcp.so.backlog";
    public static final String TCP_SO_BACKLOG_DEFAULT                = "1024";

    /**
     * KEEP ALIVE OPTION
     */
    public static final String TCP_SO_KEEP_ALIVE                        = "bolt.tcp.so.keep-alive";
    public static final String TCP_SO_KEEP_ALIVE_DEFAULT                = "true";


    /**
     * connection timeout default value,  ms
     */
    public static final int    DEFAULT_CONNECT_TIMEOUT               = 1000;

    /**
     * connections for each url
     */
    public static final int    DEFAULT_CONNECTIONS_PER_URL              = 1;

    /** max connections for each url */
    public static final int    MAX_CONNECTIONS_PER_URL                  = 100 * 10000;


    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String SERIALIZER = "dove.serializer";
    public static final String SERIALIZER_DEFAULT = "protostuff";
}
