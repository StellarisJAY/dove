package com.jay.dove.config;

/**
 * <p>
 *  Common Configs of Dove
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
    public static final boolean TCP_NODELAY_DEFAULT = true;

    /**
     * TCP SO BACKLOG OPTION
     */
    public static final String TCP_SO_BACKLOG                        = "dove.tcp.so-backlog";
    public static final int TCP_SO_BACKLOG_DEFAULT                = 1024;

    public static final String TCP_IDLE_STATE = "dove.tcp.idle-state";
    public static final boolean TCP_IDLE_STATE_DEFAULT = true;

    public static final String TCP_IDLE_TIME = "dove.tcp.idle-time";
    public static final int TCP_IDLE_TIME_DEFAULT = 10000;

    /**
     * KEEP ALIVE OPTION
     */
    public static final String TCP_SO_KEEP_ALIVE                        = "dove.tcp.so-keep-alive";
    public static final boolean TCP_SO_KEEP_ALIVE_DEFAULT                = true;


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

    /**
     * dispatch requests to processor-threads or io-threads
     */
    public static final String DISPATCH_LIST_TO_EXECUTOR = "dove.dispatch_to_executor";
    public static final boolean DISPATCH_LIST_TO_EXECUTOR_DEFAULT = false;

    /**
     * the timeout value of one connect task
     */
    public static final String CONNECT_TIMEOUT = "dove.connect_timeout";
    public static final int CONNECT_TIMEOUT_DEFAULT = 3000;

    public static final String CONNECT_MAX_RETRY_TIME = "dove.connect_max_retry";
    public static final int CONNECT_MAX_RETRY_TIME_DEFAULT = 3;

    public static final String ENABLE_RECONNECT = "dove.enable_reconnect";
    public static final boolean ENABLE_RECONNECT_DEFAULT = true;

    public static final String SERVER_MANAGE_CONNECTION = "dove.server_manage_connection";
    public static final boolean SERVER_MANAGE_CONNECTION_DEFAULT = false;

    public static boolean soKeepAlive(){
        Boolean keepAlive = ConfigManager.getBoolean(TCP_SO_KEEP_ALIVE);
        return keepAlive != null ? keepAlive : TCP_SO_KEEP_ALIVE_DEFAULT;
    }

    public static boolean tcpNoDelay(){
        Boolean config = ConfigManager.getBoolean(TCP_NODELAY);
        return config != null ? config : TCP_NODELAY_DEFAULT;
    }

    public static boolean dispatchListToExecutor(){
        Boolean config = ConfigManager.getBoolean(DISPATCH_LIST_TO_EXECUTOR);
        return config != null ? config : DISPATCH_LIST_TO_EXECUTOR_DEFAULT;
    }

    public static int connectTimeout(){
        Integer timeout = ConfigManager.getInteger(CONNECT_TIMEOUT);
        return timeout != null ? timeout : CONNECT_TIMEOUT_DEFAULT;
    }

    public static int connectMaxRetry(){
        Integer times = ConfigManager.getInteger(CONNECT_MAX_RETRY_TIME);
        return times != null ? times : CONNECT_MAX_RETRY_TIME_DEFAULT;
    }

    public static boolean tcpIdleState(){
        Boolean state = ConfigManager.getBoolean(TCP_IDLE_STATE);
        return state != null ? state : TCP_IDLE_STATE_DEFAULT;
    }

    public static int tcpIdleTime(){
        Integer time = ConfigManager.getInteger(TCP_IDLE_TIME);
        return time != null ? time : TCP_IDLE_TIME_DEFAULT;
    }

    public static boolean enableReconnect(){
        Boolean enable = ConfigManager.getBoolean(ENABLE_RECONNECT);
        return enable != null ? enable : ENABLE_RECONNECT_DEFAULT;
    }

    public static boolean serverManageConnection(){
        Boolean manage = ConfigManager.getBoolean(SERVER_MANAGE_CONNECTION);
        return manage != null ? manage : SERVER_MANAGE_CONNECTION_DEFAULT;
    }
}
