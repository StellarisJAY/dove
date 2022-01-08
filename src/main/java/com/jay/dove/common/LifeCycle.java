package com.jay.dove.common;

/**
 * <p>
 *  Common lifecycle
 * </p>
 *
 * @author Jay
 * @date 2022/01/07 15:50
 */
public interface LifeCycle {
    /**
     * start
     */
    void start();

    /**
     * init
     */
    void init();

    /**
     * stop
     */
    void shutdown();

    /**
     * exception
     * @param e exception
     */
    void exceptionCaught(Throwable e);
}
